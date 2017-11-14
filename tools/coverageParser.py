import sys
import os
import imp
import stat
import shutil

coverageBaseDir = "/dummy"

def checkParams(arguments):
    if (len(arguments) != 2):
        print "Wrong Usage Buddy!!!"
        print "Usage:",arguments[0]," <path_to_the_CompletedRuns_Directory(which usually will be present under workingDir)>"
        sys.exit(-1)

    if (not os.path.isdir(arguments[1])):
        print "Looks like the provided directory:",arguments[1]," doesn't exist"
        print "Please check this and retry"
        sys.exit(-2)

    #Copy the provided data to the global variables
    global coverageBaseDir
    coverageBaseDir = os.path.abspath(arguments[1])
    
#This is the Implemtation of 2-diemensional dictionary
class Ddict(dict):
    def __init__(self, default=None):
        self.default = default

    def __getitem__(self, key):
        if not self.has_key(key):
            self[key] = self.default()
        return dict.__getitem__(self, key)
    


# here goes the main logic of the code
checkParams(sys.argv)
completedApps = os.listdir(coverageBaseDir)
coverageDict = Ddict(dict)
totalApps = {'Name': 'Zara'}
del totalApps['Name'];
print "Coverage Report\n"
for appFolder in completedApps:
    appName = (appFolder.split('_'))[0]
    totalApps[appName] = 1
    coverageDict[appName]["final"] = "0%"
    appFinalCoverageFile = coverageBaseDir + "/" + appFolder + "/TestStrategy/RunStats/FinalCoverageStats/coverage.txt"
    if os.path.isfile(appFinalCoverageFile):
        lines = [line.strip() for line in open(appFinalCoverageFile)]
        coverageDict[appName]["final"] = lines[5].split()[6]
        
print "AppName"+"\t"+"FinalCoverage"
for eachApp in totalApps.keys():
        print eachApp + "\t" + coverageDict[eachApp]["final"]
    
         
            
            
            
    
