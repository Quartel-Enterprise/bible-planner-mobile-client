import Combine
import Shared
import SwiftUI

/// The Plans tab: the plan order, today's reading, the plan's progress and its weeks.
struct ReadingPlanView: View {
    let model: ReadingPlanScreenModel
    @State private var localization = NativeLocalization.current()

    var body: some View {
        let strings = ReadingPlanText(localization: localization)
        ReadingPlanContent(state: model.state, strings: strings, onEvent: model.send)
            .navigationTitle(strings.text("title"))
            .alert(Text(strings.text("no_progress_to_delete")), isPresented: noProgressAlertBinding) {}
            .environment(\.locale, localization.locale)
            .onReceive(
                NotificationCenter.default
                    .publisher(for: UserDefaults.didChangeNotification)
                    .receive(on: DispatchQueue.main)
            ) { _ in
                // The app language screen changes AppleLanguages while the app runs.
                let current = NativeLocalization.current()
                if current != localization {
                    localization = current
                }
            }
    }

    private var noProgressAlertBinding: Binding<Bool> {
        Binding(
            get: { model.isShowingNoProgressAlert },
            set: { isShowing in model.isShowingNoProgressAlert = isShowing }
        )
    }
}

private enum RowID {
    static let planOrder = "plan_order"
    static let hero = "hero"

    static func week(_ weekNumber: Int32) -> String {
        "week_\(weekNumber)"
    }
}

private struct ReadingPlanContent: View {
    /// Keeps the overview column readable beside the weeks on an iPad.
    private let overviewMaxWidth: CGFloat = 440

    let state: (any ReadingPlanUiState)?
    let strings: ReadingPlanText
    let onEvent: (ReadingPlanUiEvent) -> Void

    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var visibleRows: Set<String> = []

    private var isSplit: Bool {
        horizontalSizeClass == .regular
    }

    var body: some View {
        if let state {
            ScrollViewReader { proxy in
                lists(state: state)
                    .onChange(of: state.scrollToWeekNumber, initial: true) { _, weekNumber in
                        scrollToWeek(weekNumber, state: state, proxy: proxy)
                    }
                    .onChange(of: state.scrollToTop, initial: true) { _, scrollToTop in
                        guard scrollToTop else { return }
                        withAnimation {
                            proxy.scrollTo(RowID.planOrder, anchor: .top)
                        }
                        onEvent(ReadingPlanUiEventOnScrollToTopCompleted.shared)
                    }
                    .onChange(of: isActiveRowVisible(state)) { _, isVisible in
                        if isVisible != state.isActiveRowVisible {
                            onEvent(ReadingPlanUiEventOnActiveRowVisibilityChange(isActiveRowVisible: isVisible))
                        }
                    }
            }
            .toolbar { toolbarContent(state: state) }
        } else {
            ProgressView()
        }
    }

    @ViewBuilder
    private func lists(state: any ReadingPlanUiState) -> some View {
        if isSplit {
            HStack(spacing: 0) {
                List { overviewSections(state: state) }
                    .frame(maxWidth: overviewMaxWidth)
                List { weekSections(state: state) }
            }
            .listStyle(.insetGrouped)
        } else {
            List {
                overviewSections(state: state)
                weekSections(state: state)
            }
            .listStyle(.insetGrouped)
        }
    }

    // MARK: Overview

    @ViewBuilder
    private func overviewSections(state: any ReadingPlanUiState) -> some View {
        Section {
            planOrderPicker(state: state)
                .id(RowID.planOrder)
        }
        if let loaded = state as? ReadingPlanUiStateLoaded {
            Section {
                ReadingPlanHeroCard(status: loaded.planStatus, strings: strings, onEvent: onEvent)
                    .onAppear { visibleRows.insert(RowID.hero) }
                    .onDisappear { visibleRows.remove(RowID.hero) }
            }
            .listRowBackground(Color.accentColor.opacity(0.12))
            Section(strings.text("progress_title")) {
                ReadingPlanProgressRow(state: loaded, strings: strings)
            }
        } else {
            Section {
                ProgressView()
                    .frame(maxWidth: .infinity)
            }
        }
    }

    private func planOrderPicker(state: any ReadingPlanUiState) -> some View {
        let selectedPlan = state.selectedReadingPlan
        let selection = Binding<ReadingPlanType>(
            get: { selectedPlan },
            set: { plan in
                if plan != selectedPlan {
                    onEvent(ReadingPlanUiEventOnPlanClick(type: plan))
                }
            }
        )
        // Segments while both orders fit on one line, a menu otherwise, as the shared screen does.
        return ViewThatFits(in: .horizontal) {
            planOrderPicker(selection: selection)
                .pickerStyle(.segmented)
            planOrderPicker(selection: selection)
                .pickerStyle(.menu)
        }
    }

    private func planOrderPicker(selection: Binding<ReadingPlanType>) -> some View {
        Picker(strings.text("plan_order"), selection: selection) {
            ForEach(ReadingPlanType.entries, id: \.self) { plan in
                Text(strings.planTypeTitle(plan))
                    .tag(plan)
            }
        }
    }

    // MARK: Weeks

    @ViewBuilder
    private func weekSections(state: any ReadingPlanUiState) -> some View {
        if let loaded = state as? ReadingPlanUiStateLoaded {
            let isBehind = PlanModeKind(loaded.planStatus.mode) == .behind
            ForEach(ReadingPlanWeekSection.build(from: loaded)) { section in
                Section(strings.sectionTitle(section.group, isBehind: isBehind)) {
                    ForEach(section.weeks, id: \.weekPlan.number) { week in
                        ReadingPlanWeekRow(
                            week: week,
                            flashTargetGlobalIndex: loaded.flashTargetGlobalIndex,
                            strings: strings,
                            onEvent: onEvent,
                            rowID: RowID.week(week.weekPlan.number),
                            onLabelVisibilityChange: { isVisible in
                                let rowID = RowID.week(week.weekPlan.number)
                                if isVisible {
                                    visibleRows.insert(rowID)
                                } else {
                                    visibleRows.remove(rowID)
                                }
                            }
                        )
                    }
                    if let totalWeeks = section.totalWeeks {
                        showMoreButton(section: section, totalWeeks: totalWeeks, state: loaded)
                    }
                }
            }
        }
    }

    private func showMoreButton(
        section: ReadingPlanWeekSection,
        totalWeeks: Int,
        state: ReadingPlanUiStateLoaded
    ) -> some View {
        let isExpanded = section.group == .completed ? state.completedExpanded : state.upcomingExpanded
        let title = isExpanded
            ? strings.text("see_less")
            : strings.format("see_more_weeks", max(totalWeeks - section.weeks.count, 0))
        return Button {
            onEvent(toggleEvent(for: section.group))
        } label: {
            Label(title, systemImage: isExpanded ? "chevron.up" : "chevron.down")
        }
        .buttonStyle(.borderless)
    }

    private func toggleEvent(for group: WeekGroupKind) -> ReadingPlanUiEvent {
        if group == .completed {
            return ReadingPlanUiEventOnToggleCompletedExpanded.shared
        }
        return ReadingPlanUiEventOnToggleUpcomingExpanded.shared
    }

    // MARK: Toolbar

    @ToolbarContentBuilder
    private func toolbarContent(state: any ReadingPlanUiState) -> some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            if let loaded = state as? ReadingPlanUiStateLoaded, !loaded.isActiveRowVisible {
                activeDayShortcut(mode: PlanModeKind(loaded.planStatus.mode))
            }
            Menu {
                ForEach(OverflowOption.entries, id: \.self) { option in
                    overflowButton(option: option)
                }
            } label: {
                Label(strings.text("more_options"), systemImage: "ellipsis.circle")
            }
        }
    }

    @ViewBuilder
    private func activeDayShortcut(mode: PlanModeKind) -> some View {
        if let title = strings.shortcutTitle(mode) {
            Button {
                onEvent(ReadingPlanUiEventOnGoToActiveRowClick.shared)
            } label: {
                Label(title, systemImage: strings.shortcutSymbol(mode))
            }
        }
    }

    @ViewBuilder
    private func overflowButton(option: OverflowOption) -> some View {
        if let kind = OverflowOptionKind(option) {
            Button(role: kind == .deleteProgress ? .destructive : nil) {
                onEvent(ReadingPlanUiEventOnOverflowOptionClick(option: option))
            } label: {
                Label(strings.overflowTitle(kind), systemImage: strings.overflowSymbol(kind))
            }
        }
    }

    // MARK: Scrolling

    private func isActiveRowVisible(_ state: any ReadingPlanUiState) -> Bool {
        guard let loaded = state as? ReadingPlanUiStateLoaded else { return true }
        if visibleRows.contains(RowID.hero) { return true }
        guard let activeWeekNumber = loaded.planStatus.nextDay?.weekNumber else { return false }
        return visibleRows.contains(RowID.week(activeWeekNumber))
    }

    private func scrollToWeek(_ weekNumber: Int32, state: any ReadingPlanUiState, proxy: ScrollViewProxy) {
        guard weekNumber > 0, state is ReadingPlanUiStateLoaded else { return }
        // The first load's automatic scroll keeps today's reading in view when it shares the list with the weeks.
        let scrolls = !(state.scrollToWeekIsAutomatic && !isSplit)
        // Next turn of the main queue, so the list has the week the same state just expanded.
        DispatchQueue.main.async {
            if scrolls {
                withAnimation {
                    proxy.scrollTo(RowID.week(weekNumber), anchor: .top)
                }
            }
            onEvent(ReadingPlanUiEventOnScrollToWeekCompleted.shared)
        }
    }
}

/// One group of weeks, built from the shared `buildReadingPlanWeekItems` so the grouping and the collapsed limit
/// stay the same on every platform.
struct ReadingPlanWeekSection: Identifiable {
    let group: WeekGroupKind
    var weeks: [WeekPlanPresentationModel]
    /// Set when the group has more weeks than it shows collapsed, so it gets a show more or less button.
    var totalWeeks: Int?

    var id: String {
        group.rawValue
    }

    static func build(from state: ReadingPlanUiStateLoaded) -> [ReadingPlanWeekSection] {
        var sections: [ReadingPlanWeekSection] = []
        for item in ReadingPlanListItemsKt.buildReadingPlanWeekItems(state: state) {
            if let header = item as? ReadingPlanListItemSectionHeader {
                sections.append(ReadingPlanWeekSection(group: WeekGroupKind(header.group), weeks: [], totalWeeks: nil))
            } else if let week = item as? ReadingPlanListItemWeek, !sections.isEmpty {
                sections[sections.count - 1].weeks.append(week.week)
            } else if let showMore = item as? ReadingPlanListItemShowMore, !sections.isEmpty {
                sections[sections.count - 1].totalWeeks = Int(showMore.totalWeeks)
            }
        }
        return sections
    }
}
