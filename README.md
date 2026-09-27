# Roommate Expenses & Shared Group Wallet

A full-stack solution featuring:
- **Web App (`public/`)**: Ready for instant hosting on Netlify with React, Tailwind CSS, QR Scanner & Camera Pay, Fraud PIN protection, and real-time multi-device cloud synchronization via Firebase Firestore.
- **Android App (`app/`)**: Native Android application built with Jetpack Compose, Material 3, Room local database, and QR code security.

---

## 🚀 How to Connect to GitHub & Enable Real-Time Netlify Deployment

Whenever you push changes to your GitHub repository, Netlify automatically detects the commit, rebuilds, and updates your live website in real-time (usually within 15–30 seconds).

### Step 1: Push Code to GitHub

#### Option A: Direct Push from AI Studio (Recommended)
1. In the top-right header / settings menu of AI Studio, click **Push to GitHub** or **Export**.
2. Connect your GitHub account and choose a repository name (e.g. `roommate-expenses`).
3. AI Studio will push all code and subsequent commits directly to your GitHub repo.

#### Option B: Using Git Command Line
If you downloaded the project as a ZIP or are working locally:
```bash
# 1. Initialize git
git init

# 2. Add all files and make initial commit
git add .
git commit -m "Initial commit - Roommate Expenses app"

# 3. Create a new repository on https://github.com/new (do NOT check 'Initialize with README')
# 4. Link and push to GitHub
git branch -M main
git remote add origin https://github.com/<YOUR-USERNAME>/<YOUR-REPO-NAME>.git
git push -u origin main
```

---

### Step 2: Connect Netlify to Your GitHub Repository

1. Sign up or log in at **[app.netlify.com](https://app.netlify.com)**.
2. Click **Add new site** > **Import an existing project**.
3. Select **GitHub** and grant Netlify access to your repository.
4. Select your `roommate-expenses` repository.
5. Netlify will automatically detect `netlify.toml` with the correct settings:
   - **Base directory**: *(Leave blank or `/`)*
   - **Build command**: *(Leave blank)*
   - **Publish directory**: `public`
6. Click **Deploy Site**.

Your website will be live at a URL like `https://your-site-name.netlify.app`. You can also configure a custom domain or custom site name under Site Configuration > Change Site Name.

---

### Step 3: How Real-Time Updates Work

1. **Automatic Continuous Deployment (CI/CD)**:
   - Any time you edit code and push to GitHub (`git commit -m "Update feature" && git push`), GitHub immediately notifies Netlify via webhooks.
   - Netlify pulls the changes and deploys the live version automatically without any manual intervention.
2. **Real-Time Data Syncing**:
   - The web application includes built-in Firebase Firestore real-time listeners (`onSnapshot`).
   - All room expenses, deposits, QR transactions, and wallet balances update instantly across all roommates' phones and laptops in real-time without needing a page refresh or redeployment.

---

## ⚙️ Configuration Files Included

- **`netlify.toml`**: Pre-configured publish directory (`public`) and single-page routing redirects.
- **`public/index.html`**: Complete standalone responsive web app with QR code scanning, security PIN modal, audit trails, and multi-currency support.
- **`app/`**: Android Jetpack Compose native codebase.
