#!/usr/bin/env python3
"""Generate Apple Sign in client secret (ES256 JWT) using openssl, no third-party deps.

Writes the JWT to the output path. Never prints the private key or the JWT to stdout.
"""
import base64
import json
import subprocess
import sys
import time

import os
KEY_PATH = os.environ["APPLE_AUTH_KEY_P8"]
OUT_PATH = sys.argv[1]
TEAM_ID = "TTV2A365LG"
KEY_ID = "S6T3824BDQ"
CLIENT_ID = "com.quare.bibleplanner.BiblePlanner.signin"  # Services ID (sub)
MAX_AGE_SECONDS = 180 * 24 * 3600  # 180 days (Apple max is 6 months)


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


now = int(time.time())
header = {"alg": "ES256", "kid": KEY_ID, "typ": "JWT"}
payload = {
    "iss": TEAM_ID,
    "iat": now,
    "exp": now + MAX_AGE_SECONDS,
    "aud": "https://appleid.apple.com",
    "sub": CLIENT_ID,
}
signing_input = (
    b64url(json.dumps(header, separators=(",", ":")).encode())
    + "."
    + b64url(json.dumps(payload, separators=(",", ":")).encode())
).encode()

der_sig = subprocess.run(
    ["openssl", "dgst", "-sha256", "-sign", KEY_PATH],
    input=signing_input,
    capture_output=True,
    check=True,
).stdout


def parse_der_ecdsa(der: bytes) -> bytes:
    """Convert DER-encoded ECDSA signature to raw 64-byte r||s."""
    assert der[0] == 0x30
    idx = 2
    if der[1] & 0x80:
        idx += der[1] & 0x7F

    def read_int(i):
        assert der[i] == 0x02
        length = der[i + 1]
        value = der[i + 2 : i + 2 + length]
        return value.lstrip(b"\x00").rjust(32, b"\x00"), i + 2 + length

    r, idx = read_int(idx)
    s, _ = read_int(idx)
    return r + s


jwt = signing_input.decode() + "." + b64url(parse_der_ecdsa(der_sig))
with open(OUT_PATH, "w") as f:
    f.write(jwt)
exp_str = time.strftime("%Y-%m-%d", time.localtime(now + MAX_AGE_SECONDS))
print(f"JWT written to {OUT_PATH} ({len(jwt)} chars), expires {exp_str}")
