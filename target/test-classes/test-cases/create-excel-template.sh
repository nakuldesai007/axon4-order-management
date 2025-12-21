#!/bin/bash
# Script to create Excel template using Python (if available)
# Alternative: Create manually in Excel/LibreOffice

cat << 'EOF' > /tmp/create_excel_template.py
import openpyxl
from openpyxl.styles import Font, PatternFill

# Create workbook
wb = openpyxl.Workbook()
ws = wb.active
ws.title = "Test Cases"

# Headers
headers = [
    "Test Case ID", "Test Case Name", "Description", "Day", "Step Number",
    "Action", "Parameters", "Expected Status", "Expected Result", "Enabled"
]

# Style headers
header_fill = PatternFill(start_color="366092", end_color="366092", fill_type="solid")
header_font = Font(bold=True, color="FFFFFF")

for col_idx, header in enumerate(headers, 1):
    cell = ws.cell(row=1, column=col_idx, value=header)
    cell.fill = header_fill
    cell.font = header_font

# Example test case data
test_cases = [
    # Day-1: Order Creation
    ["TC-001", "Complete Order Lifecycle", "Create new order", "Day-1", 1,
     "CREATE_ORDER", "customerId=CUST-001,customerName=John Doe,customerEmail=john@example.com,shippingAddress=123 Main St",
     "CREATED", "", "Y"],
    ["TC-001", "Complete Order Lifecycle", "Add first item", "Day-1", 2,
     "ADD_ITEM", "productId=PROD-001,productName=iPhone 15,quantity=1,price=999.99",
     "CREATED", "itemCount=1", "Y"],
    ["TC-001", "Complete Order Lifecycle", "Add second item", "Day-1", 3,
     "ADD_ITEM", "productId=PROD-002,productName=AirPods Pro,quantity=1,price=249.99",
     "CREATED", "itemCount=2", "Y"],
    ["TC-001", "Complete Order Lifecycle", "Verify order state", "Day-1", 4,
     "VERIFY_ORDER", "", "CREATED", "status=CREATED,itemCount=2,totalAmount=1249.98", "Y"],
    
    # Day-2: Order Processing
    ["TC-001", "Complete Order Lifecycle", "Confirm order", "Day-2", 1,
     "CONFIRM_ORDER", "", "CONFIRMED", "", "Y"],
    ["TC-001", "Complete Order Lifecycle", "Process order", "Day-2", 2,
     "PROCESS_ORDER", "", "PROCESSED", "", "Y"],
    ["TC-001", "Complete Order Lifecycle", "Verify processed state", "Day-2", 3,
     "VERIFY_ORDER", "", "PROCESSED", "status=PROCESSED,itemCount=2", "Y"],
    
    # Day-3: Order Shipping
    ["TC-001", "Complete Order Lifecycle", "Ship order", "Day-3", 1,
     "SHIP_ORDER", "trackingNumber=TRK123456789", "SHIPPED", "", "Y"],
    ["TC-001", "Complete Order Lifecycle", "Verify shipped state", "Day-3", 2,
     "VERIFY_ORDER", "", "SHIPPED", "status=SHIPPED", "Y"],
]

# Add test case data
for row_idx, test_case in enumerate(test_cases, 2):
    for col_idx, value in enumerate(test_case, 1):
        ws.cell(row=row_idx, column=col_idx, value=value)

# Auto-adjust column widths
for col in ws.columns:
    max_length = 0
    col_letter = col[0].column_letter
    for cell in col:
        try:
            if len(str(cell.value)) > max_length:
                max_length = len(str(cell.value))
        except:
            pass
    adjusted_width = min(max_length + 2, 50)
    ws.column_dimensions[col_letter].width = adjusted_width

# Save file
output_file = "multi-day-order-lifecycle.xlsx"
wb.save(output_file)
print(f"Excel template created: {output_file}")
EOF

# Check if Python with openpyxl is available
if command -v python3 &> /dev/null; then
    python3 -c "import openpyxl" 2>/dev/null
    if [ $? -eq 0 ]; then
        # Change to the script's directory (test-cases directory)
        # This ensures the Excel file is created in the correct location regardless of where the script is run from
        SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
        cd "$SCRIPT_DIR"
        python3 /tmp/create_excel_template.py
        echo "Excel template created successfully in: $SCRIPT_DIR"
    else
        echo "openpyxl not installed. Install with: pip3 install openpyxl"
        echo "Or create the Excel file manually using the format in README.md"
    fi
else
    echo "Python3 not found. Please create the Excel file manually."
    echo "See README.md for the required format."
fi

