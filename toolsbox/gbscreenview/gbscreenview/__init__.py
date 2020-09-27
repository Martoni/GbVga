import sys
import getopt
import serial
import logging

import pkg_resources  # part of setuptools
__version__ = pkg_resources.require('gbscreenview')[0].version

def usages():
    """ print usages """
    print("Usages:")
    print("gbscreenview [options]")
    print("-h, --help               print this help")

def main(argv):
    try:
        opts, args = getopt.getopt(argv, "h", ["help"])
    except getopt.GetoptError:
        usages()
        sys.exit(2)

    for opt, arg in opts:
        if opt in ["-h", "--help"]:
            usages()
            sys.exit(0)

    print("TODO")
