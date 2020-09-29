from setuptools import setup

setup(
    name='gbscreenview',
    version='0.1',

    description='View screenshot from Game Boy LCD signal',
    long_description='View screenshot from Game Boy LCD signal',
    url='https://github.com/Martoni/GbVga',
    author='Fabien Marteau',
    author_email='mail@fabienm.eu',
    license='MIT',
    keywords='gameboy electronic game videogame',

    # See https://pypi.python.org/pypi?%3Aaction=list_classifiers
    classifiers=[
        # Indicate who your project is intended for
        'Intended Audience :: Developers',

        # Pick your license as you wish (should match "license" above)
        'License :: OSI Approved :: MIT License',

        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.7',
    ],

    packages=['gbscreenview'],
    scripts=['bin/gbscreenview'],

    # Run-time dependencies
    install_requires=['Pillow'],
)
