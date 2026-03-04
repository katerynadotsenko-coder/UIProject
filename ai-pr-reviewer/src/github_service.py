import requests
from typing import List, Dict

class GitHubClient:
    def __init__(self, token: str, owner: str, repo: str, pr_num: str):
        if not token:
            raise ValueError("❌ GITHUB_TOKEN is missing.")
        self.headers = {
            "Authorization": f"token {token}",
            "Accept": "application/vnd.github.v3+json"
        }
        self.owner = owner
        self.repo = repo
        self.pr_num = pr_num
        self.base_url = f"https://api.github.com/repos/{owner}/{repo}"

    def get_pr_files(self) -> List[Dict]:
        url = f"{self.base_url}/pulls/{self.pr_num}/files"
        res = requests.get(url, headers=self.headers)
        res.raise_for_status()
        return res.json()

    def get_file_content(self, raw_url: str) -> str:
        res = requests.get(raw_url, headers=self.headers)
        res.raise_for_status()
        return res.text

    def post_review(self, master_general: str, all_inline: List[Dict]):
        url = f"{self.base_url}/pulls/{self.pr_num}/reviews"
        payload = {
            "event": "COMMENT",
            "body": master_general if master_general else "🤖 AI Review Complete. See inline findings.",
            "comments": all_inline
        }
        res = requests.post(url, headers=self.headers, json=payload)
        if res.ok:
            print(f"🚀 Review posted! {len(all_inline)} inline comments added.")
        else:
            print(f"❌ Error posting review: {res.text}")