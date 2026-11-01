// Compares the clones of a jscpd JSON report with the baseline. Run by scripts/cpd.sh:
//   node scripts/cpd-check.mjs <report> <baseline> [--update-baseline [--allow-new]] [--base <baseline of main>]
//
// The baseline holds, per file, how many of its lines of code are copied. Lines of a file, not clones nor pairs of
// files: with the same code in three files, which pairs jscpd reports depends on the order it reads them, and an
// edit inside a long clone can split it into two shorter ones. A file over its lines fails, and so does one under
// them, until --update-baseline lowers them, so copied code removed can't come back unnoticed.

import { appendFileSync, existsSync, readFileSync, writeFileSync } from "node:fs";
import { extname, join, relative } from "node:path";
import { fileURLToPath } from "node:url";

// Why: shorter than this once imports and comments are gone, a clone is a shared header, not copied code.
const MIN_LINES = 5;

const USAGE = "Usage: ./scripts/cpd.sh [--update-baseline [--allow-new]] [--base <baseline of main>]";

const ROOT = fileURLToPath(new URL("..", import.meta.url));

const [reportPath, baselinePath, ...flags] = process.argv.slice(2);
const options = { update: false, allowNew: false, base: null };
for (let index = 0; index < flags.length; index++) {
    const flag = flags[index];
    if (flag === "--update-baseline") {
        options.update = true;
    } else if (flag === "--allow-new") {
        options.allowNew = true;
    } else if (flag === "--base") {
        options.base = flags[++index] ?? fail("--base needs the path of a baseline");
    } else {
        fail(`Unknown argument: ${flag}\n${USAGE}`);
    }
}
if (options.allowNew && !options.update) fail(`--allow-new only goes with --update-baseline\n${USAGE}`);
if (!existsSync(reportPath)) fail(`jscpd wrote no report at ${reportPath}: set "silent" to false in .jscpd.json to see why`);

const report = JSON.parse(readFileSync(reportPath, "utf8"));
const baseline = readBaseline(baselinePath);

const KOTLIN_HEADER = /^((package|import)\s|@file:)/;
const SWIFT_IMPORT = /^(@\w+\s+)*import\s/;

// The numbers of the lines of a file that are code: no blank lines, comments, package or imports.
// Why: a block comment is only seen when `/*` starts the line, which is how both codebases write them; a tokenizer
// for comments after code or nested ones would cost more than the few lines they could miscount.
const codeLinesCache = new Map();
function codeLinesOf(path) {
    if (codeLinesCache.has(path)) return codeLinesCache.get(path);
    const header = extname(path) === ".swift" ? SWIFT_IMPORT : KOTLIN_HEADER;
    const code = new Set();
    let inComment = false;
    readFileSync(path, "utf8").split("\n").forEach((raw, index) => {
        const line = raw.trim();
        if (inComment) {
            inComment = !line.includes("*/");
        } else if (line.startsWith("/*")) {
            inComment = !line.includes("*/", 2);
        } else if (line !== "" && !line.startsWith("//") && !header.test(line)) {
            code.add(index + 1);
        }
    });
    codeLinesCache.set(path, code);
    return code;
}

function codeLinesIn(file) {
    const code = codeLinesOf(file.name);
    const lines = [];
    for (let line = file.start; line <= file.end; line++) if (code.has(line)) lines.push(line);
    return lines;
}

const pathOf = (file) => relative(ROOT, file.name);

// Why: jscpd matches tokens, not lines, so the two copies of a clone can span different line counts; the shorter one
// decides, or which file jscpd happened to list first would.
const clones = report.duplicates.filter(
    (clone) => Math.min(codeLinesIn(clone.firstFile).length, codeLinesIn(clone.secondFile).length) >= MIN_LINES,
);

const copiedLines = new Map();
for (const clone of clones) {
    for (const file of [clone.firstFile, clone.secondFile]) {
        const lines = copiedLines.get(pathOf(file)) ?? new Set();
        codeLinesIn(file).forEach((line) => lines.add(line));
        copiedLines.set(pathOf(file), lines);
    }
}
const current = Object.fromEntries([...copiedLines].map(([path, lines]) => [path, lines.size]));

const grown = Object.keys(current).filter((path) => current[path] > (baseline[path] ?? 0));
const shrunk = Object.keys(baseline).filter((path) => (current[path] ?? 0) < baseline[path]);

// Why: a renamed or moved file shows up as a new path with the same copied lines as a listed path whose file is gone.
const vanished = Object.keys(baseline)
    .filter((path) => !(path in current) && !existsSync(join(ROOT, path)))
    .map((path) => baseline[path]);
const renamed = grown.filter((path) => {
    if (path in baseline) return false;
    const index = vanished.indexOf(current[path]);
    if (index < 0) return false;
    vanished.splice(index, 1);
    return true;
});
const copied = grown.filter((path) => !renamed.includes(path));

const total = (pairs) => Object.values(pairs).reduce((sum, lines) => sum + lines, 0);

if (options.update) {
    // Why: a renamed or moved file takes its copied lines along, which is no new copied code. Anything else that grew
    // is, even when copies removed elsewhere keep the total down.
    if (copied.length > 0 && !options.allowNew) {
        reportGrown(copied);
        fail("The baseline only takes more copied code in with --allow-new, when the copy is on purpose.");
    }
    writeBaseline(current);
    console.log(`✅ ${relative(ROOT, baselinePath)}: ${total(current)} copied lines in ${Object.keys(current).length} files`);
    process.exit(0);
}

if (options.base) warnAboutGrowth(readBaseline(options.base));

reportGrown(copied);
for (const path of renamed) {
    console.log(`❌ Renamed or moved with its copied code (${current[path]} lines): ${path}`);
}
for (const path of shrunk) {
    console.log(`❌ Less copied code than the baseline lists (${current[path] ?? 0} of ${baseline[path]} lines): ${path}`);
}
if (copied.length > 0) {
    console.log("Extract the copied code into one place the callers share. Only when the copy is on purpose,");
    console.log("run ./scripts/cpd.sh --update-baseline --allow-new and say why in the pull request.");
}
if (renamed.length > 0 || shrunk.length > 0) {
    console.log("Run ./scripts/cpd.sh --update-baseline to write the renamed files and the removed copied code to the baseline.");
}
if (grown.length > 0 || shrunk.length > 0) process.exit(1);

console.log(`✅ No new copied code (${total(current)} copied lines in ${Object.keys(current).length} files, as the baseline lists)`);

function reportGrown(paths) {
    for (const path of paths) {
        console.log(`❌ New copied code in ${path}: ${current[path]} lines where the baseline lists ${baseline[path] ?? 0}`);
        for (const clone of clones.filter((clone) => [clone.firstFile, clone.secondFile].some((file) => pathOf(file) === path))) {
            console.log(`   ${pathOf(clone.firstFile)}:${clone.firstFile.start}-${clone.firstFile.end}`);
            console.log(`   ${pathOf(clone.secondFile)}:${clone.secondFile.start}-${clone.secondFile.end}\n`);
        }
    }
}

/*
 * Why: --allow-new and a renamed file are the author's call, so CI doesn't fail on them, but a reviewer must see them.
 * One annotation, since GitHub shows only ten per step, and the whole list in the job summary.
 */
function warnAboutGrowth(base) {
    const taken = Object.keys(baseline).filter((path) => baseline[path] > (base[path] ?? 0));
    if (taken.length === 0) return;
    const lines = taken.map((path) => `${path}: ${base[path] ?? 0} → ${baseline[path]} lines`);
    console.log(`::warning file=.jscpd-baseline.json::The baseline takes in copied code in ${taken.length} files, listed in the job summary`);
    lines.forEach((line) => console.log(`   ${line}`));
    if (process.env.GITHUB_STEP_SUMMARY) {
        const summary = ["### The baseline takes in copied code", "", ...lines.map((line) => `- \`${line.replace(": ", "`: ")}`), ""];
        appendFileSync(process.env.GITHUB_STEP_SUMMARY, `${summary.join("\n")}\n`);
    }
}

function readBaseline(path) {
    return existsSync(path) ? JSON.parse(readFileSync(path, "utf8")) : {};
}

function writeBaseline(files) {
    const sorted = Object.fromEntries(Object.keys(files).sort().map((path) => [path, files[path]]));
    writeFileSync(baselinePath, `${JSON.stringify(sorted, null, 2)}\n`);
}

function fail(message) {
    console.error(`❌ ${message}`);
    process.exit(1);
}
