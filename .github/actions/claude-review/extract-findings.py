"""Prints the findings array of a Claude review reply, or nothing when the reply has none.

The reply ends with the findings in a ```json fence: the last such fence is read, whatever its case,
with or without a closing fence (a reply cut short), or with the fence on the same line as the
array's end. A reply that is nothing but the array, or holds one between its first "[" and its last
"]", counts too. Usage: extract-findings.py < reply.txt
"""
import json
import re
import sys

text = sys.stdin.read()
candidates = [
    match.group(1)
    for match in re.finditer(r"```[ \t]*json[ \t]*\r?\n(.*?)(?:\r?\n[ \t]*```|\Z)", text, re.I | re.S)
]
candidates.reverse()
candidates.append(text)
first, last = text.find("["), text.rfind("]")
if 0 <= first < last:
    candidates.append(text[first:last + 1])
for candidate in candidates:
    candidate = candidate.strip()
    if candidate.endswith("```"):
        candidate = candidate[:-3].rstrip()
    try:
        value = json.loads(candidate)
    except ValueError:
        continue
    if isinstance(value, list):
        print(json.dumps(value))
        break
