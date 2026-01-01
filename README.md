# Git Submodules Management Guide

This guide explains how to add, update, and completely remove Git submodules in this project.

## 1. Adding a Submodule

### Option A: Using IntelliJ IDEA (Recommended)
1. Open the **Git** tool window (`Alt+9` or `Cmd+9`).
2. Right-click on the project root in the project tree.
3. Select **Git** -> **Submodules** -> **Add...**
4. Enter the **Repository URL** and the **Relative Path** (the folder name).
5. Click **OK**.

### Option B: Using Command Line (Cross-platform)
Run this command from the project root:
```bash
git submodule add <repository_url> <path/to/folder>
git commit -m "Add submodule in <path/to/folder>"
git push
```

---

## 2. Cloning a Project with Submodules
If you have just cloned the main repository, the submodule folders will be empty. Run the following to initialize them:

```bash
git submodule update --init --recursive --jobs 10
```

---

## 3. Updating Submodules
To pull the latest changes from the submodule's remote repository:

**Via IntelliJ:**
* Go to **Git** -> **Submodules** -> **Update...**

**Via Command Line:**
```bash
git submodule update --remote --merge
```

---

## 4. Removing a Submodule
Removing a submodule requires a few steps to clean up Git's internal tracking.

### Step 1: Remove from Index
Run this in your terminal:
```bash
git rm -f <path/to/submodule>
```

### Step 2: Delete the internal Git directory
This is where commands differ based on your Operating System:

#### **For Windows (PowerShell):**
```powershell
Remove-Item -Recurse -Force .git/modules/<path/to/submodule>
```

#### **For Linux / macOS / Git Bash:**
```bash
rm -rf .git/modules/<path/to/submodule>
```

### Step 3: Final Cleanup (Manual Check)
1. Open `.gitmodules` file in the root directory. If the section for that submodule still exists, delete it.
2. Open `.git/config` file. If the submodule section exists there, delete it.
3. Commit and push the changes:
```bash
git add .gitmodules
git commit -m "Completely remove submodule <name>"
git push
```

---