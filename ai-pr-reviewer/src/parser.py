import re
from typing import Set

class PatchParser:
    @staticmethod
    def get_valid_lines(patch: str) -> Set[int]:
        """Parses git patch to find lines added or modified (+) in the PR."""
        valid_lines = set()
        if not patch: return valid_lines
        current_line = 0
        for line in patch.split('\n'):
            if line.startswith('@@'):
                match = re.search(r'\+(\d+)', line)
                if match: current_line = int(match.group(1)) - 1
            elif line.startswith(' '): current_line += 1
            elif line.startswith('+'):
                current_line += 1
                valid_lines.add(current_line)
            elif line.startswith('-'): continue
        return valid_lines