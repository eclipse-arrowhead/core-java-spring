package eu.arrowhead.core.choreographer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
import java.lang.String;
import java.lang.Double;
import java.lang.NullPointerException;
import java.lang.IndexOutOfBoundsException;
import java.lang.IllegalArgumentException;
import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class StepResponseEvaluationTest {

	private String message;

	public class Coordinates {
		private Double x;
		private Double y;
		private Double z;

		public Coordinates(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Double getX() {
			return this.x;
		}

		public Double getY() {
			return this.y;
		}

		public Double getZ() {
			return this.z;
		}
	}

	public class TestMessage {
		private Integer id;
		private Boolean isAccessable;
		private Coordinates place;
		private List<Car> cars;

		public TestMessage(int id, boolean bool, double x, double y, double z, List<Car> list) {
			this.id = id;
			this.isAccessable = bool;
			this.place = new Coordinates(x, y, z);
			this.cars = list;
		}

		public Integer getId() {
			return id;
		}

		public Boolean getIsAccessable() {
			return this.isAccessable;
		}

		public Coordinates getPlace() {
			return this.place;
		}

		public List<Car> getCars() {
			return this.cars;
		}
	}

	public class Car {

		private String color;
		private String type;

		public Car(String c, String t) {
			this.color = c;
			this.type = t;
		}

		public String getColor() {
			return this.color;
		}

		public String getType() {
			return this.type;
		}

	}

	@Before
	public void setMessage() throws JsonProcessingException {

			ObjectMapper objectMapper = new ObjectMapper();
			List<Car> list = new ArrayList();
			list.add(new Car("yellow", "Renault"));
			list.add(new Car("blue", "Suzuki"));
			list.add(new Car("silver", "Opel"));
			double x = 3.14;
			double y = 42.1;
			double z = 66.6;
			TestMessage object = new TestMessage(1234, true, x, y, z, list);
			message = objectMapper.writeValueAsString(object);

//			ObjectMapper objectMapper = new ObjectMapper();
//			Car car = new Car("yellow", "renault");
//			Coordinates place = new Coordinates(x,y,z);
//			message = objectMapper.writeValueAsString(place);

	}

	@Test
	public void exampleMessageTest() {
		Assert.assertNotEquals(null, message);
		Assert.assertNotEquals("", message);
		Assert.assertNotEquals("error", message);
		Assert.assertEquals(
				"{\"id\":1234,\"isAccessable\":true,\"place\":{\"x\":3.14,\"y\":42.1,\"z\":66.6},\"cars\":[{\"color\":\"yellow\",\"type\":\"Renault\"},{\"color\":\"blue\",\"type\":\"Suzuki\"},{\"color\":\"silver\",\"type\":\"Opel\"}]}",
				message);
	}

	@Test
	public void getJsonValueTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		String path = "value/id";
		String result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("1234", result);

		path = "value/isAccessable";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("true", result);

		path = "map/place/value/x";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("3.14", result);

		path = "map/place/value/y";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("42.1", result);

		path = "map/place/value/z";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("66.6", result);

		path = "map/cars/array/0/value/color";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("yellow", result);

		path = "map/cars/array/1/value/color";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("blue", result);

		path = "map/cars/array/2/value/color";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("silver", result);

		path = "map/cars/array/0/value/type";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("Renault", result);

		path = "map/cars/array/1/value/type";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("Suzuki", result);

		path = "map/cars/array/2/value/type";
		result = evaluator.getJsonValue(path, message);
		Assert.assertEquals("Opel", result);
		{
		}
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void getJsonValueInvalidIndexTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		String path = "map/cars/array/3/value/type";
		evaluator.getJsonValue(path, message);
	}

	@Test(expected = NullPointerException.class)
	public void getJsonValueInvalidAttributeTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		String path = "map/cars/value/type";
		evaluator.getJsonValue(path, message);
	}

	@Test
	public void stepOutputValueTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();

		String path = "value/isAccessable";
		String threshold = "boolean:true";
		Boolean result = evaluator.stepOutputValue(message, path, threshold);
		Assert.assertEquals(true, result);

		path = "value/isAccessable";
		threshold = "boolean:false";
		result = evaluator.stepOutputValue(message, path, threshold);
		Assert.assertEquals(false, result);

		path = "value/id";
		threshold = "integer:2022";
		result = evaluator.stepOutputValue(message, path, threshold);
		Assert.assertEquals(false, result);

		path = "value/id";
		threshold = "integer:1230";
		result = evaluator.stepOutputValue(message, path, threshold);
		Assert.assertEquals(true, result);

		path = "map/cars/array/1/value/color";
		threshold = "string:cyan";
		result = evaluator.stepOutputValue(message, path, threshold);
		Assert.assertEquals(false, result);

		path = "map/cars/array/2/value/type";
		threshold = "string:Opel";
		result = evaluator.stepOutputValue(message, path, threshold);
		Assert.assertEquals(true, result);

		path = "map/place/value/x";
		threshold = "double:5.1";
		result = evaluator.stepOutputValue(message, path, threshold);
		Assert.assertEquals(false, result);

		path = "map/place/value/z";
		threshold = "double:50.1";
		result = evaluator.stepOutputValue(message, path, threshold);
		Assert.assertEquals(true, result);
		{
		}
	}

	@Test(expected = NumberFormatException.class)
	public void stepOutputValueIntegerParseExceptionTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		String path = "value/id";
		String threshold = "integer:4.5";
		evaluator.stepOutputValue(message, path, threshold);
	}

	@Test(expected = NumberFormatException.class)
	public void stepOutputValueDoubleParseExceptionTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		String path = "map/place/value/x";
		String threshold = "double:true";
		evaluator.stepOutputValue(message, path, threshold);
	}

	@Test
	public void illegalArgumentExceptionTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		try {
			String path = "map/place/value/x";
			String threshold = "double 4.2";
			evaluator.stepOutputValue(message, path, threshold);
		} catch (IllegalArgumentException ex) {
			String expected = "The threshold valuable must have two component, seperated by a \":\" character.";
			Assert.assertEquals(expected, ex.getMessage());
		}
		
		try {
			String path = "map/place/value/x";
			String threshold = "long:2222222";
			evaluator.stepOutputValue(message, path, threshold);
		} catch (IllegalArgumentException ex) {
			String expected = "Invalid threshold type. The threshold type can only be double, integer, boolean or string.";
			Assert.assertEquals(expected, ex.getMessage());
		}
		
		try {
			String path = "list/place/value/x";
			String threshold = "double:2.1";
			evaluator.stepOutputValue(message, path, threshold);
		} catch (IllegalArgumentException ex) {
			String expected = "The path can contain \"array\", \"map\" and \"value\" keys with the name or index of the attribute. ";
			Assert.assertEquals(expected, ex.getMessage());
		}
		
		try {
			String path = "map/place/value/x";
			String threshold = "double:2.1";
			evaluator.stepOutputValue(" {"+message, path, threshold);
		} catch (IllegalArgumentException ex) {
			String expected = "Unable to restore value from path";
			Assert.assertEquals(expected, ex.getMessage());
		}
	}

	@Test(expected = NullPointerException.class)
	public void thresholdIsNullTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		String path = "map/cars/value/type";
		evaluator.stepOutputValue(message, path, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void messageIsNullTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		String path = "value/isAccessable";
		String threshold = "boolean:true";
		evaluator.stepOutputValue(null, path, threshold);
	}
	
	@Test(expected = NullPointerException.class)
	public void pathIsNullTest() {
		StepResponseEvaluation evaluator = new StepResponseEvaluation();
		String threshold = "boolean:true";
		evaluator.stepOutputValue(message, null, threshold);
	}
}
