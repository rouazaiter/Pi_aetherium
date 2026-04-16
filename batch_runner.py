#!/usr/bin/env python3
import os
import sys

# Change to the portfolio directory
os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Execute the batch file - the output will be displayed directly to console
exit_code = os.system(r'C:\Users\jihen\Downloads\portfolio\execute_batch_simulation.bat')

sys.exit(exit_code >> 8)  # Shift to get the actual exit code
