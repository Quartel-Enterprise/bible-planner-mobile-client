---
name: project-storage-list-rls-anon-only
description: "Storage list() passes through RLS even on public buckets; the content bucket's SELECT policy targeted only the anon role, so signed-in users listed 0 Bible versions silently."
metadata: 
  node_type: memory
  type: project
  originSessionId: 64cd4807-654c-4271-882d-d5854ab50dd5
  modified: 2026-08-05T02:56:15.611Z
---

Supabase Storage `bucketApi.list(...)` always goes through RLS on `storage.objects`, while `downloadPublic(...)` does not (public bucket URLs bypass it). On 2026-08-04 the `content` bucket had a single SELECT policy, `Allow public read access fqilq1_0`, scoped to roles `{anon}` — so `set local role authenticated` saw **0** of 4760 objects while `anon` saw all of them. Effect: as soon as a user signed in, `BibleVersionsRemoteDataSourceImpl.getVersions()` returned an empty list (RLS yields zero rows, never an error), the repository silently kept the stale cache, and newly published Bible versions never reached the app. Remedy: scope the bucket's SELECT policy to the `public` role (covers anon + authenticated), like the `avatars` policy already does.

**Why:** RLS returning empty instead of failing makes this class of bug invisible — no exception, no log, no crash; only a list that never grows.

**How to apply:** when remote content "just doesn't update" and the objects are reachable by public URL, compare `anon` vs `authenticated` visibility with `begin; set local role <role>; select count(*) from storage.objects where bucket_id = '<bucket>'; rollback;` before suspecting the client cache. Related: [[project-remoteconfig-observe-first]].
