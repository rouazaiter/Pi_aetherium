#!/usr/bin/env python3
import subprocess, sys, os
os.chdir(r'C:\Users\jihen\Downloads\portfolio')
r = subprocess.run(['javac','SetupDTOFiles.java'],capture_output=True,text=True)
if r.returncode == 0: print("✓ Compilation successful\n")
else: print("✗ Failed\n", r.stderr); sys.exit(1)
r = subprocess.run(['java','SetupDTOFiles'],capture_output=True,text=True)
print(r.stdout)
