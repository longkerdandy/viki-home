package com.github.longkerdandy.viki.home.model;

import com.github.longkerdandy.viki.home.schema.PropertySchema;
import com.github.longkerdandy.viki.home.util.Jacksons;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;

public class PropertyTest {

  @Test
  public void IntegerTest() throws IOException {
    PropertySchema<Long> schema = PropertySchema.createIntegerProperty(
        "level", null, 1L, 10L, 3L,
        new Long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L});
    Property<Long> property = new Property<>("level", DataType.INTEGER, 3L);

    assert property.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(property);
    assert json != null;
    property = Jacksons.getReader(Property.class).readValue(json);
    assert property.getName().equals("level");
    assert property.getType() == DataType.INTEGER;
    assert property.getValue() == 3L;
    assert property.validate(schema);
  }

  @Test
  public void NumberTest() throws IOException {
    PropertySchema<Double> schema = PropertySchema.createNumberProperty(
        "hp", null, 1.0d, 10.0d, 3.3d,
        new Double[]{1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d, 8.8d, 9.9d});
    Property<Double> property = new Property<>("hp", DataType.NUMBER, 3.3d);

    assert property.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(property);
    assert json != null;
    property = Jacksons.getReader(Property.class).readValue(json);
    assert property.getName().equals("hp");
    assert property.getType() == DataType.NUMBER;
    assert property.getValue() == 3.3d;
    assert property.validate(schema);
  }

  @Test
  public void StringTest() throws IOException {
    PropertySchema<String> schema = PropertySchema.createStringProperty(
        "level", null, 1, 10, null, "3",
        new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
    Property<String> property = new Property<>("id", DataType.STRING, "3");

    assert property.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(property);
    assert json != null;
    property = Jacksons.getReader(Property.class).readValue(json);
    assert property.getName().equals("id");
    assert property.getType() == DataType.STRING;
    assert property.getValue().equals("3");
    assert property.validate(schema);
  }

  @Test
  public void BooleanTest() throws IOException {
    PropertySchema<Boolean> schema = PropertySchema.createBooleanProperty("on", null, null);
    Property<Boolean> property = new Property<>("on", DataType.BOOLEAN, true);

    assert property.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(property);
    assert json != null;
    property = Jacksons.getReader(Property.class).readValue(json);
    assert property.getName().equals("on");
    assert property.getType() == DataType.BOOLEAN;
    assert property.getValue();
    assert property.validate(schema);
  }

  @Test
  public void DateTimeTest() throws IOException {
    PropertySchema<LocalDateTime> schema = PropertySchema.createDateTimeProperty(
        "time", null, null, LocalDateTime.now(),
        LocalDateTime.of(1983, 5, 16, 17, 0));
    Property<LocalDateTime> property = new Property<>("time", DataType.DATETIME,
        LocalDateTime.of(1983, 5, 16, 17, 0));

    assert property.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(property);
    assert json != null;
    property = Jacksons.getReader(Property.class).readValue(json);
    assert property.getName().equals("time");
    assert property.getType() == DataType.DATETIME;
    assert property.getValue().equals(
        LocalDateTime.of(1983, 5, 16, 17, 0));
    assert property.validate(schema);
  }

  @Test
  public void ArrayTest() throws IOException {
    PropertySchema<Long> items = PropertySchema.createIntegerProperty(
        "item", null, 1L, 10L, null,
        new Long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L});
    PropertySchema<long[]> schema = PropertySchema.createIntegerArrayProperty(
        "items", null, 1, 10, items, null);
    Property<long[]> property = new Property<>("items", DataType.ARRAY_INTEGER,
        new long[]{3L, 6L, 9L});

    assert property.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(property);
    assert json != null;
    property = Jacksons.getReader(Property.class).readValue(json);
    assert property.getName().equals("items");
    assert property.getType() == DataType.ARRAY_INTEGER;
    assert Arrays.equals(property.getValue(), new long[]{3L, 6L, 9L});
    assert property.validate(schema);
  }

  @Test
  public void BlobTest() throws IOException {
    PropertySchema<byte[]> schema = PropertySchema.createBlobProperty(
        "image", null, 1, 10, null);
    Property<byte[]> property = new Property<>("image",
        DataType.BLOB, new byte[]{(byte) 0xe0, (byte) 0x4f, (byte) 0xd0, (byte) 0x20});

    assert property.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(property);
    assert json != null;
    property = Jacksons.getReader(Property.class).readValue(json);
    assert property.getName().equals("image");
    assert property.getType() == DataType.BLOB;
    assert Arrays.equals(property.getValue(),
        new byte[]{(byte) 0xe0, (byte) 0x4f, (byte) 0xd0, (byte) 0x20});
    assert property.validate(schema);
  }

  @Test
  public void ObjectTest() throws IOException {
    PropertySchema<Double> brightness = PropertySchema.createNumberProperty(
        "brightness", null, 0.0d, 100.0d, null, null);
    PropertySchema<Long> color = PropertySchema.createIntegerProperty(
        "color", null, 0L, 255L, null, null);
    PropertySchema<long[]> rgb = PropertySchema.createIntegerArrayProperty(
        "rgb", null, 3, 3, color, null);
    PropertySchema schema = PropertySchema.createObjectProperty(
        "lamp", null, Map.of("brightness", brightness, "rgb", rgb));

    Property<Property[]> property = new Property<>("lamp", DataType.OBJECT,
        new Property[]{new Property<>("brightness", DataType.NUMBER, 66.6d),
            new Property<>("rgb", DataType.ARRAY_INTEGER, new long[]{255, 125, 125})});

    assert property.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(property);
    assert json != null;
    property = Jacksons.getReader(Property.class).readValue(json);
    assert property.getName().equals("lamp");
    assert property.getType() == DataType.OBJECT;
    assert (Double) property.getValue()[0].getValue() == 66.6d;
    assert Arrays.equals((long[]) property.getValue()[1].getValue(), new long[]{255, 125, 125});
    assert property.validate(schema);
  }
}
