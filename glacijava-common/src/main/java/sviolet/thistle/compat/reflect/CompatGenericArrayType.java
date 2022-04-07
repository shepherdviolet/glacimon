package sviolet.thistle.compat.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Get source code from sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl (JDK 8)
 */
public class CompatGenericArrayType implements GenericArrayType {

    private final Type genericComponentType;

    private CompatGenericArrayType(Type componentType) {
        this.genericComponentType = componentType;
    }

    public static CompatGenericArrayType make(Type componentType) {
        return new CompatGenericArrayType(componentType);
    }

    public Type getGenericComponentType() {
        return this.genericComponentType;
    }

    public String toString() {
        Type componentType = this.getGenericComponentType();
        StringBuilder stringBuilder = new StringBuilder();
        if (componentType instanceof Class) {
            stringBuilder.append(((Class<?>)componentType).getName());
        } else {
            stringBuilder.append(componentType.toString());
        }

        stringBuilder.append("[]");
        return stringBuilder.toString();
    }

    public boolean equals(Object type) {
        if (type instanceof GenericArrayType) {
            GenericArrayType var2 = (GenericArrayType)type;
            return Objects.equals(this.genericComponentType, var2.getGenericComponentType());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hashCode(this.genericComponentType);
    }
}
