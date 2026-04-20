# ZLogin

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![Platform](https://img.shields.io/badge/Platform-Spigot%20%7C%20Paper%20%7C%20Purpur-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**ZLogin** is a lightweight and efficient Minecraft authentication plugin designed to provide maximum security without interrupting the gameplay flow. It prioritizes **IP-to-Account Binding** using advanced hashing mechanisms to ensure player accounts remain secure while making the login process seamless.

---

## Key Features

* **Ultra-Lightweight:** Built with performance in mind to ensure zero impact on server TPS (Ticks Per Second).
* **IP-to-Account Attachment:** Intelligently links a player's IP address to their specific account.
* **Secure IP Hashing:** Utilizes robust hashing algorithms to store IP data, ensuring player privacy and data protection (GDPR compliant).
* **Auto-Login System:** Recognized players don't need to re-enter passwords as long as they join from a trusted/verified connection.
* **Session Management:** Securely handles player sessions to prevent account hijacking and unauthorized access.

## How It Works

ZLogin operates behind the scenes with the following logic:
1.  **Hashing:** When a player joins, the plugin captures the IP address and converts it into a unique, irreversible hash string.
2.  **Matching:** The system compares this hash against the database entries associated with the player's UUID.
3.  **Verification:** If the hashes match, access is granted instantly. If a discrepancy is detected (e.g., joining from a new network), the system triggers a manual authentication request.

## Installation

1.  Download the latest `ZLogin.jar` from the [Releases](../../releases) tab.
2.  Place the file into the `/plugins` folder of your Minecraft server directory.
3.  Start or restart your server.
4.  Customize your preferences in `/plugins/ZLogin/config.yml`.

