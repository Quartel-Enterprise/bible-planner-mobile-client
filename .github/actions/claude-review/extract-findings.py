"""Prints the findings array of a Claude review reply, or nothing when the reply has none.

The reply ends with the findings in a ```json fence: the last such fence is read, whatever its case,
with or without a closing fence (a reply cut short), or with the fence on the same line as the
array's end. A reply that is nothing but the array counts too. Usage: extract-findings.py < reply.txt
"""
import json
import re
import sys

text = sys.stdin.read()
opening = re.compile(r"^[ \t]*```[ \t]*json[ \t]*$", re.I)
closing = re.compile(r"^[ \t]*```[ \t]*$")
candidates = []
block = None
for line in text.splitlines():
    if block is None:
        if opening.match(line):
            block = []
    elif closing.match(line):
        candidates.append("\n".join(block))
        block = None
    elif line.rstrip().endswith("```"):
        block.append(line.rstrip()[:-3])
        candidates.append("\n".join(block))
        block = None
    else:
        block.append(line)
if block is not None:
    candidates.append("\n".join(block))
candidates.reverse()
candidates.append(text)
for candidate in candidates:
    try:
        value = json.loads(candidate.strip())
    except ValueError:
        continue
    if isinstance(value, list):
        print(json.dumps(value))
        break
