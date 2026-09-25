package com.quare.bibleplanner.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Update
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sf.symbols.SFSymbol

enum class AppIcon(
    internal val material: () -> ImageVector,
    val sfSymbol: String,
) {
    AccountCircle(
        material = Icons.Default::AccountCircle,
        sfSymbol = SFSymbol.personCropCircleFill,
    ),
    ArrowForward(
        material = Icons.AutoMirrored.Filled::ArrowForward,
        sfSymbol = SFSymbol.arrowForward,
    ),
    AutoAwesome(
        material = Icons.Default::AutoAwesome,
        sfSymbol = SFSymbol.sparkles,
    ),
    Bolt(
        material = Icons.Default::Bolt,
        sfSymbol = SFSymbol.boltFill,
    ),
    Check(
        material = Icons.Default::Check,
        sfSymbol = SFSymbol.checkmark,
    ),
    CheckCircle(
        material = Icons.Default::CheckCircle,
        sfSymbol = SFSymbol.checkmarkCircleFill,
    ),
    ChevronForward(
        material = Icons.AutoMirrored.Filled::KeyboardArrowRight,
        sfSymbol = SFSymbol.chevronForward,
    ),
    Close(
        material = Icons.Default::Close,
        sfSymbol = SFSymbol.xmark,
    ),
    Delete(
        material = Icons.Default::Delete,
        sfSymbol = SFSymbol.trash,
    ),
    DeleteSweep(
        material = Icons.Default::DeleteSweep,
        sfSymbol = SFSymbol.xmarkBin,
    ),
    Description(
        material = Icons.Default::Description,
        sfSymbol = SFSymbol.textDocument,
    ),
    Edit(
        material = Icons.Default::Edit,
        sfSymbol = SFSymbol.pencil,
    ),
    EditCalendar(
        material = Icons.Default::EditCalendar,
        sfSymbol = SFSymbol.calendarBadgeClock,
    ),
    Error(
        material = Icons.Default::Error,
        sfSymbol = SFSymbol.exclamationmarkCircleFill,
    ),
    ExpandLess(
        material = Icons.Default::ExpandLess,
        sfSymbol = SFSymbol.chevronUp,
    ),
    ExpandMore(
        material = Icons.Default::ExpandMore,
        sfSymbol = SFSymbol.chevronDown,
    ),
    Favorite(
        material = Icons.Default::Favorite,
        sfSymbol = SFSymbol.heartFill,
    ),
    FilterList(
        material = Icons.Default::FilterList,
        sfSymbol = SFSymbol.line3HorizontalDecrease,
    ),
    GridView(
        material = Icons.Default::GridView,
        sfSymbol = SFSymbol.squareGrid2x2,
    ),
    History(
        material = Icons.Default::History,
        sfSymbol = SFSymbol.clockArrowTriangleheadCounterclockwiseRotate90,
    ),
    Info(
        material = Icons.Default::Info,
        sfSymbol = SFSymbol.infoCircleFill,
    ),
    KeyboardArrowDown(
        material = Icons.Default::KeyboardArrowDown,
        sfSymbol = SFSymbol.chevronDown,
    ),
    KeyboardArrowUp(
        material = Icons.Default::KeyboardArrowUp,
        sfSymbol = SFSymbol.chevronUp,
    ),
    Language(
        material = Icons.Default::Language,
        sfSymbol = SFSymbol.globe,
    ),
    ListView(
        material = Icons.AutoMirrored.Filled::List,
        sfSymbol = SFSymbol.listBullet,
    ),
    LocalFireDepartment(
        material = Icons.Default::LocalFireDepartment,
        sfSymbol = SFSymbol.flameFill,
    ),
    Logout(
        material = Icons.AutoMirrored.Filled::Logout,
        sfSymbol = SFSymbol.rectanglePortraitAndArrowRight,
    ),
    MenuBook(
        material = Icons.AutoMirrored.Filled::MenuBook,
        sfSymbol = SFSymbol.book,
    ),
    MilitaryTech(
        material = Icons.Default::MilitaryTech,
        sfSymbol = SFSymbol.medalFill,
    ),
    MoreVert(
        material = Icons.Default::MoreVert,
        sfSymbol = SFSymbol.ellipsis,
    ),
    OpenInNew(
        material = Icons.AutoMirrored.Filled::OpenInNew,
        sfSymbol = SFSymbol.arrowUpRightSquare,
    ),
    Palette(
        material = Icons.Default::Palette,
        sfSymbol = SFSymbol.paintpalette,
    ),
    PersonRemove(
        material = Icons.Default::PersonRemove,
        sfSymbol = SFSymbol.personBadgeMinus,
    ),
    PlayArrow(
        material = Icons.Default::PlayArrow,
        sfSymbol = SFSymbol.playFill,
    ),
    RateReview(
        material = Icons.Default::RateReview,
        sfSymbol = SFSymbol.starBubble,
    ),
    Schedule(
        material = Icons.Default::Schedule,
        sfSymbol = SFSymbol.clock,
    ),
    Search(
        material = Icons.Default::Search,
        sfSymbol = SFSymbol.magnifyingglass,
    ),
    SortByAlpha(
        material = Icons.Default::SortByAlpha,
        sfSymbol = SFSymbol.arrowUpArrowDown,
    ),
    Star(
        material = Icons.Default::Star,
        sfSymbol = SFSymbol.starFill,
    ),
    SupportAgent(
        material = Icons.Default::SupportAgent,
        sfSymbol = SFSymbol.questionmarkBubble,
    ),
    Today(
        material = Icons.Default::Today,
        sfSymbol = SFSymbol.calendar,
    ),
    Translate(
        material = Icons.Default::Translate,
        sfSymbol = SFSymbol.translate,
    ),
    TrendingUp(
        material = Icons.AutoMirrored.Filled::TrendingUp,
        sfSymbol = SFSymbol.chartLineUptrendXyaxis,
    ),
    Update(
        material = Icons.Default::Update,
        sfSymbol = SFSymbol.arrowDownApp,
    ),
}
