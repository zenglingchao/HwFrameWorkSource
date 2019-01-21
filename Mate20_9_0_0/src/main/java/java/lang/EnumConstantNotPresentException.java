package java.lang;

public class EnumConstantNotPresentException extends RuntimeException {
    private static final long serialVersionUID = -6046998521960521108L;
    private String constantName;
    private Class<? extends Enum> enumType;

    public EnumConstantNotPresentException(Class<? extends Enum> enumType, String constantName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(enumType.getName());
        stringBuilder.append(".");
        stringBuilder.append(constantName);
        super(stringBuilder.toString());
        this.enumType = enumType;
        this.constantName = constantName;
    }

    public Class<? extends Enum> enumType() {
        return this.enumType;
    }

    public String constantName() {
        return this.constantName;
    }
}
