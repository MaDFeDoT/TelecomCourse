TEMPLATE = app
CONFIG += console c++11
CONFIG -= app_bundle
CONFIG -= qt

SOURCES += main.c
QMAKE_CXXFLAGS += -std=c++0x -pthread 
LIBS += -pthread
