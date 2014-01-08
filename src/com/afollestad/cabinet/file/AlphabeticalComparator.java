package com.afollestad.cabinet.file;

/**
 * Sorts files and folders by name, alphabetically.
 *
 * @author Aidan Follestad (afollestad)
 */
class AlphabeticalComparator implements java.util.Comparator<File> {

    @Override
    public int compare(File lhs, File rhs) {
        return lhs.getName().compareToIgnoreCase(rhs.getName());
    }
}