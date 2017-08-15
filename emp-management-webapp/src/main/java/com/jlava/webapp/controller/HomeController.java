package com.jlava.webapp.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;

import java.util.Map;
import java.util.List;
import java.util.Collections;

import com.jlava.service.PersonManager;
import com.jlava.model.Person;

@Controller
@SessionAttributes({"person"})
public class HomeController {
	private final PersonManager personManager;

	public HomeController(PersonManager personManager) {
		this.personManager = personManager;
	}
	
	@RequestMapping(value={"/","/home"}, method=GET)
	public String loadHome(ModelMap model) {
		return "index";
	}

	@RequestMapping(value={"/populate_list"}, method=GET)
	public @ResponseBody List<Person> sortList(@RequestParam("sortBy") int sortBy, ModelMap model){
		return personManager.listPersons(sortBy);
	}

	@RequestMapping(value="/home", params={"search"}, method=POST)
	public String searchEmployee(@RequestParam("searchId") Long searchId, ModelMap model){
		String searchResult = "";
		if(searchId != null) {
			try {
				Person person = personManager.getPerson(searchId);
				if(person != null) {
					//add to flash attribute, redirect to manage-person controller
					model.addAttribute("person", person);
					return "redirect:manage-person";
				} else {
					searchResult = "Person with id " + searchId + " not found";
				}
			} catch(Exception e) {
				searchResult = "Invalid Id";
			}
		} 

		model.addAttribute("searchResult", searchResult);
		return "index";
	}

	@RequestMapping(value="/home/{searchId}", method=GET)
	public String getEmployee(@PathVariable Long searchId, ModelMap model){
		String searchResult = "";
		if(searchId != null) {
			try {
				Person person = personManager.getPerson(searchId);
				if(person != null) {
					//add to flash attribute, redirect to manage-person controller
					model.addAttribute("person", person);
					return "redirect:/manage-person";
				} else {
					searchResult = "Person with id " + searchId + " not found";
				}
			} catch(Exception e) {
				searchResult = "Invalid Id";
			}
		} 

		model.addAttribute("searchResult", searchResult);
		return "index";
	}

	@RequestMapping(value="/delete_person", method=GET)
	public @ResponseBody Map<String, String> deleteEmployee(@RequestParam("delete") Long deleteId, ModelMap model){
		String deleteResult = "";

		if(deleteId != null) {
			try {
				int deleted = personManager.deletePerson(deleteId);
				deleteResult = 	(deleted > 0)?"Successfully deleted person with id = " + deleteId:"Cannot delete person with id = " + deleteId;
			} catch(Exception e) {
				deleteResult = "Person with id " + deleteId + " does not exist";				
			}
		} 

		return Collections.singletonMap("deleteResult", deleteResult);
	}
}