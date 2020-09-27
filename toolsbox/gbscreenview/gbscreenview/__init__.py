import sys
import getopt
import serial
import logging
from .GbScreenView import GbScreenView

import pkg_resources  # part of setuptools
__version__ = pkg_resources.require('gbscreenview')[0].version

def usages():
    """ print usages """
    print("Usages:")
    print("gbscreenview [options]")
    print("-h, --help               print this help")
    print("-c, --csv filename.csv   give csv filename")

def main(argv):
    try:
        opts, args = getopt.getopt(argv, "hc:",
                ["help", "csv="])
    except getopt.GetoptError:
        usages()
        sys.exit(2)

    csv = None
    for opt, arg in opts:
        if opt in ["-h", "--help"]:
            usages()
            sys.exit(0)
        elif opt in ["-c", "--csv"]:
            csv = arg

    gsv = GbScreenView()
    gsv.read_csv(csv)
    gsv.show()
