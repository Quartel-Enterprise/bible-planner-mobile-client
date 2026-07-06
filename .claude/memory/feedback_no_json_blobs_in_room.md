---
name: feedback_no_json_blobs_in_room
description: Never persist JSON blobs in Room columns; model proper normalized entities/relations instead.
metadata: 
  node_type: memory
  type: feedback
  originSessionId: f714df8f-2051-4f5c-ae02-d93ec768a068
---

Do not store serialized JSON strings in Room columns ("salvar json no banco é meio trash"). Model the data as proper entities with foreign keys + Index and read them back with @Relation POJOs (@Transaction query), sorting child lists by an explicit `position` column.

**Why:** Pierre rejected the `DayStudyEntity.contentJson` design and asked for specific entities (day_studies + child tables for chapter summaries, takeaways, facts, questions).

**How to apply:** parent entity + one child entity per list, `ForeignKey(onDelete = CASCADE)`, `Index("parentKey")`, `position` column for ordering, DAO `@Transaction` replace method (upsert parent, delete+insert children). Uncommitted schema changes can be reshaped without bumping the DB version again.
