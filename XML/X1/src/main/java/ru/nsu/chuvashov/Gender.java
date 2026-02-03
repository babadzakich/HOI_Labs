package ru.nsu.chuvashov;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum Gender {
    @XmlEnumValue("MALE")
    MALE,

    @XmlEnumValue("FEMALE")
    FEMALE
}
