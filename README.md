# 🗭 Expense & Income Tracker - Product Roadmap

This roadmap is structured for efficient development of an advanced expense/income tracking application with features like borrowing/lending, tagging, sub-categorization, and rich analytics.

---

## ✅ Phase 1: Foundation - Data Modeling & Sync

### 🔹 Task 1.1: Data Model Refactor (**Done ✅**)
- Refactor data classes:
  - `Payment`, `Payee`, `Reminder`, etc. now include fields like `type`, `tag`, `linkedToTag`, `categoryId`, `subCategoryId`

### 🔹 Task 1.2: Firestore Structure Finalization  (**Done ✅**)
- Define Firestore collections:
  - `/payments`, `/categories`, `/users`, `/accounts`, `/reminders`, `/payees`
  - `/categories/{categoryId}/subcategories/{subCategoryId}`
- Define indexing & query structure to support filters

### in 1.2 What’s Remaining (**Minimal**)
  - Firestore Indexes Setup (in Firebase Console)
  - Optional Soft Delete or Archived Flag


### 🔹 Task 1.3: Update Firestore Security Rules
- Enforce ownership checks
- Ensure queryable filters (e.g. category, tag)
- Handle soft-deletes if needed

---

## 🔶 Phase 2: Category/Sub-Category Management (**Unblocks Filtering & UI Structure**)

### 🔹 Task 2.1: Category Management Screen
- Add/Edit/Delete Categories
- Add/Edit/Delete Sub-Categories
- Display in hierarchical list (e.g., ExpandableItems)

### 🔹 Task 2.2: Store Categories in Firestore
- `/categories/{categoryId}` and nested subcategories

### 🔹 Task 2.3: Use in Transaction Forms
- Dropdown selectors for category & sub-category
- Auto-load available options from Firestore

---

## 🔶 Phase 3: Tagging & Linking Engine (**Unblocks Borrow/Lent UX**)

### 🔹 Task 3.1: Tag Generation Logic
- Generate incremental tags: `#EX01`, `#IN01`, `#BR01`, etc.
- Store counters in Firestore or locally (e.g., `/metadata/tag_counters`)

### 🔹 Task 3.2: Tag Selection in Form
- Auto-assign tag if new transaction
- Provide UI to select linked tag (for repayments)

### 🔹 Task 3.3: Tag-Linked Transactions
- In transaction detail screen, show related payments via `linkedToTag`
- Sectioned UI like "Repayments for #BR01"

---

## 🔷 Phase 4: Advanced Transaction Features

### 🔹 Task 4.1: Type-based Entry UI
- Support `income`, `expense`, `borrow`, `lent`
- Change fields dynamically based on type

### 🔹 Task 4.2: Payee/Person Entry Support
- Name/contact info for borrow/lent
- Link person info to transaction via `payeeId`

### 🔹 Task 4.3: Inline Reminders
- Auto-suggest reminders for borrow/lent
- Optional due-date notification support

---

## 🔹 Phase 5: All Transactions UI Enhancements

### 🔹 Task 5.1: Filters
- Date range
- Category / Sub-category
- Transaction type
- Tag search

### 🔹 Task 5.2: Sticky Header Grouping
- Group transactions by date
- Show total per day with header

### 🔹 Task 5.3: Pagination & Infinite Scroll
- Handle page state cleanly on re-entry
- Prevent duplicate loads

---

## 🟢 Phase 6: Final Touches

### 🔹 Task 6.1: Dashboard Summary
- Show total spent, earned, remaining
- Insights for current month / year

### 🔹 Task 6.2: Export/Backup
- Allow exporting payments (e.g. CSV)

### 🔹 Task 6.3: Role-Based Access
- Admin approval of users
- Limit category edits to admins (optional)

---

## 🧹 Optional Future Enhancements

- AI-based auto-categorization
- OCR receipt scanning
- Offline-first support
- Multi-currency support

---

### 🔄 Suggested Development Order:
1. Data model + Firestore rules ✅
2. Category/Sub-category management
3. Tag generation + linking
4. Repayment linkage + details view
5. Filters and All Transaction screen updates
6. Dashboard & analytics

---

> You can keep checking off completed tasks in this file or sync with a project board.

---

