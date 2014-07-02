package controllers;

import java.util.Map;

import play.mvc.Controller;

public class ParentController extends Controller {
	public static String getFormValue(String name) {
		String value = null;
		if (request() == null)
			return value;

		if (request().body() == null)
			return value;

		Map<String, String[]> form = request().body().asFormUrlEncoded();
		if (form == null)
			return value;

		String[] values = form.get(name);
		if (values == null || values.length <= 0)
			return value;

		value = values[0];
		return value;
	}
}
