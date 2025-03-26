# Steam Account Automation Tool

## Overview
The **Steam Account Automation Tool** is a Java-based application that programmatically completes all required form fields and handles account verification steps to streamline the creation of new Steam accounts. It integrates multiple SMS API services for phone verification, uses **KeyAuth** for secure user authentication, and leverages **IMAP** to manage email verification. The application is also **multi-threaded** to enhance efficiency and scalability.

## Features
- **Automated Registration**  
  - Fully automates the Steam account creation process, minimizing manual input.
- **KeyAuth Integration**  
  - Implements a secure authentication layer to ensure that only authorized users can access the tool’s functionality.
- **Multi-SMS API Integration**  
  - Integrates three SMS APIs to handle phone verifications, offering fallback and rotation logic for high availability.
- **IMAP Email Handling**  
  - Uses IMAP to automatically retrieve verification emails and extract necessary confirmation codes.
- **Multi-threading**  
  - Creates and verifies multiple accounts in parallel, significantly reducing overall processing time.
- **Java Swing UI**  
  - Offers a user-friendly interface for real-time monitoring, configuration, and logging.

## Prerequisites
- **Java 8** or higher (JDK recommended)
- **Internet Connection** for Steam interactions, SMS API calls, and email retrieval
- **SMS API Credentials** (keys or tokens) from your chosen SMS providers
- **KeyAuth Credentials** (application credentials and user login details)
- **Email Account** with IMAP enabled for verification retrieval

## Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/steam-account-automation-tool.git
   cd steam-account-automation-tool
