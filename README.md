# Lamesa
A restaurant manager built using Java and JavaFX, it is a desktop application to provide a management system for Lamesa.

---

# Features 

### Meals

- Add, update, and delete meals
- Assign meal categories and types
- search meals quickly

### Category Management 

 - Create and manage categories 
 - Automatically reflected in the meals panel

### Sales Tracking 

- Record customer orders
- Track quantity sold
- View top-selling meals

### SQLite Database Integration 

- Local file-based storage
- Minimal setup required

### JavaFX UI 

- Clean and responsive interface
- Multi-page navigation

----

# Tech Stack 

| Component            | Technology                                                  |
| -------------------- | ----------------------------------------------------------- |
| Programming Language | **Java** (JDK 17+)                                          |
| UI Framework         | **JavaFX**                                                  |
| Database             | **SQLite**                                                  |
| Build Tool           | **Maven** or manual JAR packaging (depending on your setup) |


---

# Installation & Setup 

### Clone Repository

```bash
git clone https://github.com/yourusername/lamesa.git
cd lamesa
```

### Libraries using Maven 

ensure your pom.xml includes the required dependencies. If using a local setup (intelliJ), make sure your javaFX SDK is linked. 

### SQlite Database 

Lamesa will automatically create the database file if it doesn't exist.

```bash
database/lamesa.db
```

--- 

# Employee & Manager Use Cases 

- Employee - Only has access of the following:
  
 - Dashboard
 - Inventory
 - Food Menu
 - Analytics

---

- Manager - Has specific access of the following:
 - Employees: Staff management features

     
--- 

# How To Run 

If using Maven:

- a script is also available in the root folder called watch.bat

```bash
mvn clean install
mvn javafx:run
```

if running manually: 

```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar Lamesa.jar
```

--- 


## Code Structure

### Directory Layout

```
lamesa/
├── src/
│   ├── main/
│   │   ├── java/main/
│   │   │   ├── App.java                      # Entry point
│   │   │   ├── SessionManager.java           # Session management
│   │   │   ├── AttendanceUtils.java          # Attendance tracking
│   │   │   ├── EmployeeDAO.java              # Database access
│   │   │   ├── Controllers/
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   ├── MainController.java
│   │   │   │   ├── MenuController.java
│   │   │   │   ├── FoodMenuController.java
│   │   │   │   ├── FoodMenuDialogController.java
│   │   │   │   ├── InventoryController.java
│   │   │   │   ├── AnalyticsController.java
│   │   │   │   ├── EmployeesController.java
│   │   │   │   ├── EditEmployeeController.java
│   │   │   │   ├── ChoiceController.java
│   │   │   │   ├── SettingsController.java
│   │   │   │   └── NewStockDialogController.java
│   │   │   ├── Models/
│   │   │   │   ├── FoodMenuItem.java
│   │   │   │   ├── InventoryItem.java
│   │   │   │   └── Employee.java
│   │   │   ├── module-info.java              # Module definition
│   │   │   └── ...
│   │   ├── resources/
│   │   │   ├── main/                         # FXML files
│   │   │   │   ├── login.fxml
│   │   │   │   ├── register.fxml
│   │   │   │   ├── main.fxml
│   │   │   │   ├── dashboard.fxml
│   │   │   │   ├── food_menu.fxml
│   │   │   │   ├── food_menu_dialog.fxml
│   │   │   │   ├── inventory.fxml
│   │   │   │   ├── analytics.fxml
│   │   │   │   ├── employees.fxml
│   │   │   │   ├── edit_employee.fxml
│   │   │   │   ├── choice.fxml
│   │   │   │   ├── menu.fxml
│   │   │   │   ├── newstock-dialog.fxml
│   │   │   │   ├── settings.fxml
│   │   │   │   └── ...
│   │   │   ├── styles/                       # CSS files
│   │   │   │   ├── login.css
│   │   │   │   ├── main.css
│   │   │   │   ├── food_menu.css
│   │   │   │   ├── inventory.css
│   │   │   │   ├── analytics.css
│   │   │   │   ├── employees.css
│   │   │   │   └── ...
│   │   │   └── assets/                       # Images & resources
│   │   │       ├── appicon.png
│   │   │       ├── meal_*.png
│   │   │       └── ...
│   │   └── test/java/                        # Unit tests (if added)
│   │
├── database/
│   └── lamesa.db                             # SQLite database
│
├── pom.xml                                   # Maven configuration
├── watch.bat                                 # Quick run script (Windows)
└── README.md                                 # Project overview
```

---


## Getting Started

### First Time Setup

#### Step 1: Launch the Application

**Windows:**
1. Open File Explorer
2. Navigate to the Lamesa folder
3. Double-click `watch.bat` (or run via command line: `mvn javafx:run`)

The login screen will appear.

#### Step 2: Create Your Account (First User Only)

1. Click **"Don't have an account? Register here"** link at the bottom
2. Fill in the registration form:
   - **Username:** Enter a unique username (3+ characters, no spaces)
   - **Password:** Enter a strong password (8+ characters)
     - Must include: uppercase letter, lowercase letter, number, special character
     - Example: `RestaurantPro123!`
   - **Confirm Password:** Re-enter your password
3. Click **"Create Account"** button
4. You'll see a success message
5. Click **"Back to Login"** to return to login screen

#### Step 3: Log In

1. Enter your **Username** and **Password**
2. Click **"Login"** or press **Enter** key
3. You'll be logged in and see the Dashboard

---

## Dashboard Overview

### Main Screen After Login

When you log in, you'll see the Dashboard with:

- **Top Navigation Bar** - Shows your username and logout button
- **Left Sidebar Menu** - Quick access to all features
- **Main Content Area** - Current page content
- **User Info** - Your role (Manager or Employee)

### Main Menu Options

| Menu Item | Purpose | Who Can Access |
|-----------|---------|----------------|
| **Dashboard** | Overview and quick access | Everyone |
| **Food Menu** | Add, edit, view meals | Everyone |
| **Inventory** | Manage stock & supplies | Everyone |
| **Employees** | View staff & attendance | Managers |
| **Analytics** | Sales reports & trends | Everyone |
| **Settings** | Account & preferences | Everyone |

### How to Navigate

1. Click any option in the **left sidebar** to navigate to that section
2. The selected option will be highlighted
3. Content changes in the main area
4. Click **Logout** at the top right to exit

---

## Food Menu Management

The Food Menu section lets you manage restaurant menu items with prices, descriptions, and images.

### View All Meals

**Steps:**
1. Click **"Food Menu"** in the sidebar
2. You'll see a grid of meal cards
3. Each card shows:
   - Meal image
   - Meal name
   - Price
   - Category and type badges

### Search for a Meal

**Steps:**
1. Go to **Food Menu** page
2. Use the **search field** at the top
3. Type the meal name (e.g., "pizza", "salad")
4. Results update in real-time as you type
5. Clear the search to see all meals

### Filter Meals by Category

**Available Categories:**
- Breakfast
- Lunch
- Dinner
- Snack
- Dessert
- Drinks

**Steps:**
1. Go to **Food Menu** page
2. Look for **category toggle buttons** at the top
3. Click the button for the category you want (e.g., "Breakfast")
4. Multiple selections allowed - all selected categories display
5. Click again to deselect
6. Results update instantly

### Filter Meals by Type

**Available Types:**
- Vegetarian
- Non-Vegetarian

**Steps:**
1. Go to **Food Menu** page
2. Look for **type toggle buttons**
3. Click **"Vegetarian"** or **"Non-Vegetarian"** or both
4. Only meals matching selected types display
5. Click to toggle on/off

### Add a New Meal

**Steps:**
1. Go to **Food Menu** page
2. Click the **"Add New"** button (usually top right)
3. A dialog window opens with fields:

   **Field Descriptions:**
   - **Image Upload Area:** Click the gray box to upload meal image
   - **Meal Name:** Enter the name (e.g., "Grilled Salmon")
   - **Category:** Select from dropdown (Breakfast, Lunch, etc.)
   - **Type:** Select from dropdown (Vegetarian, Non-Vegetarian)
   - **Price:** Enter numeric price (e.g., 12.50)
   - **Description:** Write a description (optional)

4. **To Upload Image:**
   - Click the image upload area
   - Select a PNG or JPG file from your computer
   - Image preview appears
   - Image is automatically saved to the system

5. Click **"Add Meal"** to save
6. You'll see a success message
7. Dialog closes and new meal appears in menu

### Edit an Existing Meal

**Steps:**
1. Go to **Food Menu** page
2. Find the meal card you want to edit
3. Click on the meal card
4. Click **"Edit"** button
5. The Add Meal dialog opens with current data filled in
6. Modify any fields:
   - Change name, price, description
   - Upload a new image
   - Change category or type
7. Click **"Update Meal"** to save changes
8. Success message confirms update

### Delete a Meal

**Steps:**
1. Go to **Food Menu** page
2. Find the meal you want to delete
3. Click on the meal card
4. Click **"Delete"** button
5. A confirmation dialog appears asking "Are you sure?"
6. Click **"Yes"** to confirm deletion
7. Meal is removed from menu
8. Click **"No"** to cancel

---

## Inventory Management

The Inventory section helps you track stock levels for ingredients and supplies.

### View All Inventory Items

**Steps:**
1. Click **"Inventory"** in the sidebar
2. You'll see a table with all inventory items
3. Columns show:
   - Product ID
   - Product Name
   - Category
   - Type
   - Instructions
   - Stock Quantity
   - Status (Available / Action Required)
   - Date Added

### Search Inventory

**Steps:**
1. Go to **Inventory** page
2. Use the **search field** at the top
3. Type the product name (e.g., "tomatoes", "milk")
4. Table filters to show matching items
5. Clear search to see all items

### Filter by Status

**Status Options:**
- **Available** - Sufficient stock
- **Action Required** - Low stock (needs reordering)
- **All** - Show everything

**Steps:**
1. Go to **Inventory** page
2. Click the **"Status Filter"** button
3. Each click cycles through: All → Available → Action Required → All
4. Table updates to show matching items
5. Item status appears in the Status column

### Edit Stock Quantity

**Steps:**
1. Go to **Inventory** page
2. Find the item you want to edit
3. Click on the **Stock Quantity** cell for that item
4. The cell becomes editable (turns into a text field)
5. Clear the current number and type the new quantity
6. Press **Enter** to save
7. Status automatically updates:
   - Quantity > 10 = "Available"
   - Quantity ≤ 10 = "Action Required"

### Add New Stock Item

**Steps:**
1. Go to **Inventory** page
2. Click **"New Stock"** button (top right)
3. A dialog opens with fields:

   **Field Descriptions:**
   - **Product Name:** Name of item (e.g., "Cherry Tomatoes")
   - **Category:** Type of item (e.g., "Produce", "Dairy", "Meat")
   - **Type:** Specific type (e.g., "Vegetables", "Liquids")
   - **Instructions:** How to use/store (optional)
   - **Initial Quantity:** Starting stock count

4. Fill in all required fields
5. Click **"Add Item"** button
6. Item is added to inventory table
7. Status automatically set based on quantity

### Delete Stock Items

**Steps:**
1. Go to **Inventory** page
2. **Check the checkbox** next to items you want to delete
3. You can select multiple items
4. Click the **"Delete"** button
5. A confirmation dialog appears
6. Click **"Yes"** to confirm
7. Selected items are removed from inventory

### Low Stock Alert

**Automatic Alert System:**
- When stock quantity drops to 10 or below
- Item status automatically changes to **"Action Required"**
- Color-coded in red for easy visibility
- Use Status Filter to see all items needing attention

---

## Employee Management

View and manage restaurant staff, attendance, and roles.

### View All Employees

**Steps:**
1. Click **"Employees"** in the sidebar (Managers only)
2. You'll see a table with all staff members
3. Columns show:
   - Username
   - Role (Manager or Employee)
   - Last Login (date and time)
   - Shift Status (On Shift / Off Shift / Never Logged In)

### Search Employees

**Steps:**
1. Go to **Employees** page
2. Use the **search field** on the left
3. Type employee username
4. Table filters to show matching employees
5. Search is case-insensitive

### Filter by Role

**Available Roles:**
- All
- manager
- employee

**Steps:**
1. Go to **Employees** page
2. Click the **Role Filter** dropdown
3. Select desired role:
   - **All** - Show all employees
   - **manager** - Show managers only
   - **employee** - Show employees only
4. Table updates automatically
5. Can combine with search filter

### Check Employee Shift Status

**Shift Status Meanings:**
- **On Shift** - Employee logged in but hasn't logged out
- **Off Shift** - Employee logged out (shift completed)
- **Never Logged In** - No attendance record yet

**Steps:**
1. Go to **Employees** page
2. Check the "Shift Status" column
3. Status shows current state for each employee
4. Scroll right if column is not visible

### Change Employee Role (Managers Only)

**Steps:**
1. Go to **Employees** page
2. Find the employee you want to edit
3. Click on their row
4. Click **"Edit"** or **"Edit Role"** button
5. A dialog opens showing:
   - Current username
   - Current role dropdown
6. Click the role dropdown
7. Select new role:
   - **manager** - Grant manager permissions
   - **employee** - Standard employee permissions
8. Click **"Update"** or **"Save"** button
9. Success message confirms change
10. Employee's role updates in table

### View Attendance History

**Steps:**
1. Go to **Employees** page
2. Click on an employee's name
3. Attendance history may appear (if available)
4. Shows:
   - Login times
   - Logout times
   - Hours worked
   - Shift completion status

---

## Sales Analytics

Track sales performance, trends, and revenue metrics.

### View Sales Dashboard

**Steps:**
1. Click **"Analytics"** in the sidebar
2. You'll see several sections:

   **Top Section - Key Metrics:**
   - Monthly Sales Total
   - Yearly Sales Total
   - Large, easy-to-read numbers

   **Chart Section:**
   - Line Chart - Sales trend over time
   - Pie Chart - Distribution by meal type
   - Bar Chart - Top-selling meals

### Check Monthly Sales Total

**Steps:**
1. Go to **Analytics** page
2. Look for **"Total Monthly Sales"** section at the top
3. Shows total revenue for current month
4. Updates automatically

### Check Yearly Sales Total

**Steps:**
1. Go to **Analytics** page
2. Look for **"Total Yearly Sales"** section
3. Shows total revenue for current year
4. Updates automatically

### View Sales Trend (Line Chart)

**What It Shows:**
- Sales over time (days, weeks, or months)
- Visual trend line
- High and low sales periods

**Steps:**
1. Go to **Analytics** page
2. Find the **Line Chart** (top right area)
3. Look at the trend:
   - **Upward line** = sales increasing
   - **Downward line** = sales decreasing
   - **Flat line** = stable sales
4. Hover over points for specific dates/amounts (if available)

### View Sales Distribution (Pie Chart)

**What It Shows:**
- What percentage of sales each meal represents
- Popular vs. less popular items

**Steps:**
1. Go to **Analytics** page
2. Find the **Pie Chart** (center area)
3. Each colored section = one meal type
4. Larger sections = more popular meals
5. Look at the legend to identify meals

### View Top-Selling Items (Bar Chart)

**What It Shows:**
- Which meals are selling best
- Sales volume or revenue for each meal

**Steps:**
1. Go to **Analytics** page
2. Find the **Bar Chart** (bottom area)
3. Taller bars = better sellers
4. Meal names on left, sales amount on bottom
5. Read top to bottom for ranking

### Filter by Custom Date Range

**Steps:**
1. Go to **Analytics** page
2. Find the **date picker fields:**
   - Start Date Picker (left)
   - End Date Picker (right)
3. Click the **Start Date** field
4. Calendar appears - select start date
5. Click the **End Date** field
6. Calendar appears - select end date
7. Charts and totals update to show data for that range
8. Reset to current month by clicking default buttons (if available)

---

## Settings & Account

Manage your account preferences and personal settings.

### View Your Account Information

**Steps:**
1. Click **"Settings"** in the sidebar
2. Your account details appear:
   - Username
   - Role (Manager or Employee)
   - Account creation date
   - Other preferences

### Change Password

**Note:** Password change feature may be in development. Check your Settings page for password change option.

**If Available:**
1. Go to **Settings** page
2. Click **"Change Password"** button
3. Enter current password
4. Enter new password (8+ characters, mixed case, numbers, special char)
5. Confirm new password
6. Click **"Update"** to save
7. Success message confirms change

### View Work Hours

**Steps:**
1. Go to **Settings** page
2. Look for **"My Attendance"** or **"Work Hours"** section
3. Shows your attendance records:
   - Login times
   - Logout times
   - Hours worked per shift
   - Full shift completion status

### View Your Statistics

**Steps:**
1. Go to **Settings** page
2. Look for **"My Statistics"** section
3. May show:
   - Total hours worked this month
   - Total shifts completed
   - Average shift length
   - Attendance record

---

## Tips & Best Practices

### Data Entry Tips

#### Meal Names
- Use clear, descriptive names
- Example: "Grilled Chicken Breast with Lemon Sauce" (not just "Chicken")
- Include key ingredients if helpful

#### Prices
- Always enter prices correctly (include decimals)
- Example: 12.50 (not 12 or 12,50)
- Regular audits help catch pricing errors

#### Images
- Use high-quality, clear photos
- Recommended size: 300x300 pixels or larger
- Formats: PNG or JPG
- Show food attractively for better customer appeal

#### Inventory Instructions
- Be specific about storage
- Example: "Refrigerate below 40°F, use within 3 days"
- Include preparation notes if applicable

### Daily Workflow Checklist

**Morning:**
- [ ] Log in to Lamesa
- [ ] Check **Inventory** for low stock items (Action Required)
- [ ] Review **yesterday's sales** in Analytics
- [ ] Brief team on daily specials

**Throughout Day:**
- [ ] Keep inventory updated as stock is used
- [ ] Record all meal sales (if manual entry required)
- [ ] Note any staff attendance issues

**End of Shift:**
- [ ] Log out (closes your shift)
- [ ] Verify daily sales figures match actual transactions
- [ ] Check for any inventory items needing reorder

**Weekly (Managers):**
- [ ] Review **Analytics** trends
- [ ] Check employee attendance records
- [ ] Identify top-selling and slow-moving items
- [ ] Plan next week's specials

**Monthly:**
- [ ] Review **monthly sales** goals vs. actual
- [ ] Analyze **top-selling meals**
- [ ] Update inventory categories if needed
- [ ] Review employee performance

### Security Best Practices

#### Account Security
- Never share your username/password
- Use a strong password (mix of characters)
- Change password regularly (monthly recommended)
- Log out before leaving the computer
- Log out from all devices if password changes

#### Data Protection
- Managers: Don't share sensitive employee data
- Back up database regularly (IT responsibility)
- Report suspicious activity immediately
- Never modify historical data without approval

#### Session Management
- Click **Logout** at end of shift (records attendance)
- Never leave computer unattended while logged in
- Close browser window after logout
- Use auto-logout if available (usually 30 minutes of inactivity)

---

## Frequently Asked Questions

### Login & Account

**Q: I forgot my password. What do I do?**
A: Contact your manager or system administrator. They may be able to reset it. Note: This version doesn't have automatic password reset - manual reset is required.

**Q: Can I change my username?**
A: No, usernames are permanent. If you need a new username, contact your manager to create a new account.

**Q: What happens to my data when I log out?**
A: Your logout time is recorded automatically. All data is saved to the database. Your session ends securely.

---

### Food Menu

**Q: Can I upload multiple images for one meal?**
A: No, each meal has one main image. If you need to update the image, edit the meal and upload a new one.

**Q: What image formats are supported?**
A: PNG and JPG files. Images should be at least 300x300 pixels for best quality.

**Q: Can I copy a meal instead of creating from scratch?**
A: Not currently. You'll need to manually enter similar meals. Consider using keyboard shortcuts to speed up entry.

**Q: Where are meal images stored?**
A: Images are saved in the application's asset folder. They're linked to meals via file path.

**Q: Can I delete a meal if it's been sold before?**
A: Yes, you can delete any meal. Historical sales records remain intact even after deleting the meal from the menu.

---

### Inventory

**Q: How often should I update inventory?**
A: Update when items are used, delivered, or spoiled. Best practice: daily stocktake at end of shift.

**Q: What happens when stock reaches "Action Required"?**
A: The item is flagged as low. Managers should reorder. Current threshold: 10 units or below.

**Q: Can I adjust stock quantities in bulk?**
A: Currently, you edit one item at a time. Select multiple items for deletion, but quantities must be edited individually.

**Q: What if I enter the wrong quantity?**
A: Simply click the quantity cell again and correct it. Changes save immediately.

**Q: Can I see who made changes to inventory?**
A: Not in this version. Future versions may add audit logging.

---

### Employees & Attendance

**Q: How is shift time calculated?**
A: Time from login to logout. The system calculates hours automatically in decimal format (e.g., 8.5 hours).

**Q: What if an employee forgets to log out?**
A: A manager can manually edit the record or record a manual logout. Contact your system administrator for help.

**Q: What is "full shift"?**
A: A completed shift of required duration (currently 8+ hours). For testing, it's 10 seconds. Marks as 1 (yes) or 0 (no).

**Q: Can employees see each other's hours?**
A: No, attendance data is manager-only. Employees only see their own info in Settings.

**Q: Why can't I see the Employees section?**
A: Only managers have access to employee management. Request manager privileges if needed.

---

### Analytics

**Q: How often do sales numbers update?**
A: In real-time. When a sale is recorded, charts update automatically.

**Q: Can I export reports?**
A: Not in this version. You can take screenshots of charts. Future versions may include PDF export.

**Q: Why is a meal showing $0 in sales?**
A: It hasn't been sold yet, or sales data hasn't been entered. Check that sales are being recorded.

**Q: What time period does "this month" cover?**
A: Calendar month (1st to last day). "This year" is January 1 to December 31.

**Q: Can I compare two different months?**
A: Use the date range picker to select custom dates for comparison.

---

### General Troubleshooting

**Q: The app won't start. What do I do?**
A: 
1. Ensure Java is installed (java -version in terminal)
2. Try closing and reopening
3. Restart your computer
4. Contact IT support if problems persist

**Q: I see an error message. What does it mean?**
A: Read the error carefully. Common issues:
- "Database locked" - Another user is accessing data. Wait a moment and retry.
- "Resource not found" - App files may be corrupted. Reinstall required.
- "Connection failed" - Database file missing. Contact IT.

**Q: Why is my search not working?**
A: 
- Ensure you've typed correctly (search is case-insensitive)
- Try clearing the field and retyping
- Refresh the page or navigate away and back

**Q: Why did I get logged out suddenly?**
A: Possible reasons:
- Another user logged in with your account
- Session expired after inactivity
- System administrator logged you out
- Application crashed

---

### Best Practices Q&A

**Q: How should I organize meal categories?**
A: Use standard categories:
- Breakfast (morning items)
- Lunch (midday meals)
- Dinner (evening meals)
- Snack (small items)
- Dessert (sweet items)
- Drinks (beverages)

**Q: What's a good inventory reorder point?**
A: Set "Action Required" status when stock is low enough to warrant reordering but high enough to cover demand. Consider:
- Average daily usage
- Supplier delivery time
- Storage space
- Item perishability

**Q: How do I track meal popularity?**
A: Use the Analytics page:
1. Check the Pie Chart for sales distribution
2. View the Bar Chart for top sellers
3. Compare this month vs. last month
4. Use data to plan specials and promotions

**Q: Should I keep historical sales data?**
A: Yes. Historical data is valuable for:
- Trend analysis
- Seasonal planning
- Decision-making
- Legal compliance
- Never delete sales records

---

## Getting Help

### Need Support?

If you encounter issues not covered in this manual:

1. **Check FAQ Section** - Your question may be answered above
2. **Contact Your Manager** - They may have additional training
3. **Check Documentation** - See DOCUMENTATION.md for technical details
4. **Contact IT Support** - For system issues or bugs

### Provide Helpful Information

When reporting issues, include:
- What you were trying to do
- What happened instead
- Error messages (if any)
- When it happened
- Which page/feature was affected

---

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Submit Login | Enter |
| Submit Registration | Enter |
| Submit Dialog | Enter |
| Close Dialog | Escape |
| Clear Search | Ctrl+A, Delete |

---

**Remember:** Regular updates to the system may add new features. Check with your manager for the latest training updates.




# License

For academic and personal use only. 
Feel free to modify and improve it.












