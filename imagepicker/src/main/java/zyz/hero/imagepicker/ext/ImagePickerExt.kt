package zyz.hero.imagepicker.ext

import zyz.hero.imagepicker.ImagePicker

fun pickResource(
    config: ImagePicker.Builder.() -> Unit = {},
) = ImagePicker.Builder().apply(config).build()

