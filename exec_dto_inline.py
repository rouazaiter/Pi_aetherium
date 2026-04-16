#!/usr/bin/env python3
"""
DTO Creator - Inline Execution
This script creates the dto directory and Java DTO files
All output will be captured and displayed below
"""

if __name__ == "__main__":
    import os
    import sys
    
    # Change working directory
    os.chdir(r"C:\Users\jihen\Downloads\portfolio")
    
    # Now execute create_dto_final.py 
    exec(open("create_dto_final.py").read())
