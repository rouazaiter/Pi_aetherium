#!/usr/bin/env python3
import os
import subprocess
import sys

def main():
    # Change to the portfolio directory
    os.chdir(r'C:\Users\jihen\Downloads\portfolio')
    
    # Run the batch file and capture output
    result = subprocess.run(
        [r'C:\Users\jihen\Downloads\portfolio\create_dto_batch_now.bat'],
        capture_output=True,
        text=True,
        shell=True
    )
    
    # Print stdout and stderr
    print(result.stdout)
    if result.stderr:
        print("STDERR:", result.stderr, file=sys.stderr)
    
    sys.exit(result.returncode)

if __name__ == '__main__':
    main()
