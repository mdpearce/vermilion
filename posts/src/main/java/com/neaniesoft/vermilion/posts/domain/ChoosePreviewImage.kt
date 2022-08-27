package com.neaniesoft.vermilion.posts.domain

import com.neaniesoft.vermilion.coreentities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.AnimatedImagePreview

fun List<UriImage>.choosePreviewImage(): UriImage? = maxByOrNull { it.width }

fun List<AnimatedImagePreview>.choosePreviewImage(): AnimatedImagePreview? =
    maxByOrNull { it.width }
