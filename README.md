# Steam Account Automation Tool

## Overview
The **Steam Account Automation Tool** is a Java-based application designed to streamline the process of creating Steam accounts. By programmatically completing all required form fields and handling verification steps, it greatly reduces the manual effort typically involved in setting up new accounts. The tool integrates with multiple SMS API services to automatically attach phone numbers for verification, ensuring a reliable and efficient account creation workflow.

## Features
- **Automated Account Creation**  
  Programmatically fills out all necessary form fields on Steam, reducing the need for manual input.
  
- **Multi-SMS API Integration**  
  Integrates three separate SMS API services to attach phone numbers for verification, with built-in fallback and rotation logic for improved reliability.
  
- **Java Swing UI**  
  Includes a user-friendly interface built with Java Swing to manage generation settings, monitor progress, and handle SMS workflow in real time.

## Requirements
- **Java**: JDK 8 or later
- **Maven** or **Gradle** (optional, depending on your build system)
- **Internet Connection**: Required for SMS API calls and Steam interaction
- **SMS API Keys**: You’ll need valid API keys/credentials for the SMS services you plan to use

## Installation
1. **Clone the Repository**  
   ```bash
   git clone https://github.com/yourusername/steam-account-automation-tool.git
   cd steam-account-automation-tool
