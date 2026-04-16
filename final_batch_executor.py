#!/usr/bin/env python3
"""Execute the batch file and capture complete output"""

import subprocess
import os
import sys

def main():
    os.chdir(r'C:\Users\jihen\Downloads\portfolio')
    
    # Execute the batch file
    try:
        result = subprocess.run(
            ['cmd.exe', '/c', r'C:\Users\jihen\Downloads\portfolio\create_dto_batch_now.bat'],
            capture_output=True,
            text=True,
            timeout=30
        )
        
        output = result.stdout + result.stderr
        
        # Write to output file
        with open('batch_execution_output.txt', 'w') as f:
            f.write(output)
        
        # Also print to console
        print(output)
        
        return result.returncode
        
    except subprocess.TimeoutExpired:
        print("ERROR: Batch file execution timed out")
        return 1
    except Exception as e:
        print(f"ERROR: {e}")
        return 1

if __name__ == '__main__':
    sys.exit(main())
