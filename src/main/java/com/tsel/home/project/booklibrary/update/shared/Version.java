package com.tsel.home.project.booklibrary.update.shared;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Version(BigInteger major, BigInteger minor, BigInteger patch, String suffix) implements Comparable<Version> {

    private static final Pattern FORMAT = Pattern.compile("v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)(?:-([0-9A-Za-z.-]+))?");

    public static Version parse(String value) {
        Matcher matcher = FORMAT.matcher(value == null ? "" : value);
        if (value == null || value.length() > 100 || !matcher.matches()) {
            throw new IllegalArgumentException("Некорректный номер версии: " + value);
        }
        return new Version(new BigInteger(matcher.group(1)), new BigInteger(matcher.group(2)),
            new BigInteger(matcher.group(3)), matcher.group(4));
    }

    @Override
    public int compareTo(Version other) {
        int comparison = major.compareTo(other.major);
        if (comparison == 0) {
            comparison = minor.compareTo(other.minor);
        }
        if (comparison == 0) {
            comparison = patch.compareTo(other.patch);
        }
        if (comparison != 0) {
            return comparison;
        }
        if (suffix == null || other.suffix == null) {
            return suffix == other.suffix ? 0 : suffix == null ? 1 : -1;
        }
        String[] left = suffix.split("\\.");
        String[] right = other.suffix.split("\\.");
        for (int i = 0; i < Math.min(left.length, right.length); i++) {
            boolean leftNumeric = left[i].matches("[0-9]+");
            boolean rightNumeric = right[i].matches("[0-9]+");
            comparison = leftNumeric && rightNumeric
                ? new BigInteger(left[i]).compareTo(new BigInteger(right[i]))
                : leftNumeric != rightNumeric ? (leftNumeric ? -1 : 1) : left[i].compareTo(right[i]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(left.length, right.length);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (suffix == null ? "" : "-" + suffix);
    }
}
