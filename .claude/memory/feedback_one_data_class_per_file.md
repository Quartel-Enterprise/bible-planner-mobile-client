---
name: feedback_one_data_class_per_file
description: Every data class (and enum/class declaration in general) must live in its own file; never group several data classes in one file.
metadata: 
  node_type: memory
  type: feedback
  originSessionId: f714df8f-2051-4f5c-ae02-d93ec768a068
---

Every data class must have its own file — do not group multiple data classes (DTOs, domain models, UI models) in a single .kt file, even when they are small and closely related.

**Why:** Pierre corrected a batch of files I created (DayStudy.kt with 5 data classes, DTO files with several @Serializable classes, UiState file with model+enum) and asked for one file per data class.

**How to apply:** when creating models/DTOs/UI models, produce one file per declaration named after the class. Related to [[feedback_uistate_no_defaults]].
