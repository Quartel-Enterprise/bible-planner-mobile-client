package com.quare.bibleplanner.feature.chapterstudy.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.chapter_study.generated.resources.Res
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_exhausted_subtitle
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_generate
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_generate_hint
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_generate_hint_pro
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_hero_title
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_pro_badge
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_quota_free
import bibleplanner.feature.chapter_study.generated.resources.chapter_study_subscribe
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyHeroUiModel
import com.quare.bibleplanner.ui.component.study.AiStudyBadge
import com.quare.bibleplanner.ui.component.study.AiStudyHeroContent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChapterStudyHeroContent(
    hero: ChapterStudyHeroUiModel,
    isStarting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AiStudyHeroContent(
        icon = if (hero.isLocked) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome,
        title = stringResource(Res.string.chapter_study_hero_title),
        subtitle = when {
            hero.isLocked -> stringResource(
                Res.string.chapter_study_exhausted_subtitle,
                hero.quota?.freeLimit ?: 0,
            )

            hero.isPro -> stringResource(Res.string.chapter_study_generate_hint_pro)

            else -> stringResource(Res.string.chapter_study_generate_hint)
        },
        buttonIcon = if (hero.isLocked) Icons.Rounded.LockOpen else Icons.Rounded.AutoAwesome,
        buttonLabel = stringResource(
            if (hero.isLocked) Res.string.chapter_study_subscribe else Res.string.chapter_study_generate,
        ),
        isLoading = isStarting,
        onClick = onClick,
        modifier = modifier,
        badge = { ChapterStudyHeroBadge(hero) },
    )
}

@Composable
private fun ChapterStudyHeroBadge(hero: ChapterStudyHeroUiModel) {
    val freeLeft = hero.quota
        ?.takeUnless { it.isUnlocked }
        ?.remainingFree
        ?.takeIf { it > 0 }
    when {
        hero.isPro || hero.isLocked -> AiStudyBadge(
            text = stringResource(Res.string.chapter_study_pro_badge),
            isAccent = true,
        )

        freeLeft != null -> AiStudyBadge(
            text = stringResource(Res.string.chapter_study_quota_free, freeLeft),
            isAccent = false,
        )
    }
}
