package com.jlava.webapp.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.jlava.service.PersonManager;
import com.jlava.service.ContactManager;
import com.jlava.service.RoleManager;
import com.jlava.model.Person;
import com.jlava.model.Role;
import com.jlava.model.Contact;
import com.jlava.model.ContactType;
import com.jlava.apputil.AppUtil;

@Controller
@SessionAttributes({"person"})
public class PersonController {
	private PersonManager personManager;
	private ContactManager contactManager;
	private RoleManager roleManager;

	public PersonController(PersonManager personManager, ContactManager contactManager, RoleManager roleManager) {
		this.personManager = personManager;
		this.contactManager = contactManager;
		this.roleManager = roleManager;
	}

	@RequestMapping(value="/add-person", method=GET)
	public ModelAndView addEmployee() {
		return new ModelAndView("add-person", "person", new Person());
	}

	@RequestMapping(value="/add-person", method=POST)
	public String addEmployee(@ModelAttribute("person") Person person, BindingResult result, ModelMap model) {
		Long id = personManager.addPerson(person);
		String addResult = "";

		if(id > 0) {
			person.setId(id);
			System.out.println("PERSON ID : " + id);

		} else {
			System.out.println("ADD FAILED");
			// return error
		}

		model.addAttribute("person", person);
		model.addAttribute("addResult", addResult);
		return "manage-person";
	}

	@RequestMapping(value="/upload-person", method=POST)
	public @ResponseBody String uploadEmployee(@RequestParam("empProfile") MultipartFile empProfile, ModelMap model) {
		String message = "";
		if(empProfile.isEmpty()) {
			message = "No file attached";
		} else {
			try {
				String content = new String(empProfile.getBytes(), "UTF-8");
				Person person = personManager.getPerson(personManager.addPerson(content));
				model.addAttribute("person", person);

				return "manage-person";
			} catch(Exception e) {
				message = "Invalid file attached";
				System.out.println("error : " + e.getMessage());
				e.printStackTrace();
			}
		}

		return message;
	}

	//Search Person
	@RequestMapping(value="/manage-person", method=GET)
	public String searchPerson(ModelMap model) {
		if(!model.containsAttribute("person")) {
			return "redirect:home";
		}

		List<Role> roles = roleManager.listRoles();
		List<ContactType> contactTypes = contactManager.getContactTypes();

		model.addAttribute("roles", roles);
		model.addAttribute("contacts", contactTypes);

		return "manage-person";
	}


	//Update Person
	@RequestMapping(value="/manage-person", method=POST)
	public String updatePerson(@ModelAttribute("person") Person person, 
		@RequestParam Map<String,String> allRequestParams, 
		ModelMap model, RedirectAttributes redirectAttributes) {
		boolean updated = false;
		
		// remove deleted roles
		for(Role role : new ArrayList<Role>(person.getRoles())) {
			if(role.isDeleted()) {
				person.getRoles().remove(role);
			}
		}

		// add roles and contacts
		for(Map.Entry<String,String> param : allRequestParams.entrySet()) {
			if(param.getKey().equals("newRoleId")) {
				Long roleId = Long.valueOf(param.getValue());
				Role role = new Role(allRequestParams.get("newRoleCode-"+roleId), allRequestParams.get("newRoleDesc-"+roleId));
				role.setId(roleId);
				role.setDeleted(false);

				person.getRoles().add(role);
			}

			if(param.getKey().equals("newContact")) {
				Long typeId = Long.valueOf(param.getValue());
				ContactType type = contactManager.getContactType(typeId);
				String contactDesc = allRequestParams.get("newContactDesc-"+typeId);

				Contact contact = new Contact(type, contactDesc);
				contact.setPerson(person);
				person.getContacts().add(contact);
			}
		}


		int result = personManager.updatePerson(person);

		// remove deleted contacts
		for(Contact contact : new ArrayList<Contact>(person.getContacts())) {
			if(contact.isDeleted()) {
				person.getContacts().remove(contact);
			}
		}

		if(result > 0) {
			redirectAttributes.addFlashAttribute("goodUpdate", "Update Successful");
		} else {
			redirectAttributes.addFlashAttribute("badUpdate", "Update Failed");
		}
		model.addAttribute("person", person);
		return "redirect:manage-person";
	}

	@RequestMapping(value="/get-role", method=GET)
	public @ResponseBody Role validateRole(@RequestParam("roleId") Long roleId, ModelMap model) {
		Person person = (Person) model.get("person");
		Role role = roleManager.getRole(roleId);

		for(Role pRole : person.getRoles()) {
			if(role.getId() == pRole.getId()) {
				return null;
			}
		}

		return role;
	}

	@RequestMapping(value="/get-contact-type", method=GET)
	public @ResponseBody ContactType getContactType(@RequestParam("typeId") Long typeId, @RequestParam("contactInfo") String contactInfo, ModelMap model) {
		String contactDesc = "";
		//validate desc
		try{
			switch(typeId.intValue()) {
				case 1:
					contactDesc = AppUtil.readNumeric(contactInfo, false, 7, "Phone");
					break;
				case 2:
					contactDesc = AppUtil.readNumeric(contactInfo, false, 11, "Cellphone");
					break;
				case 3:
				default:
					contactDesc = AppUtil.readLine(contactInfo, false, 30, "Email");
					break;
			}
		} catch(Exception e) {
			return null;
		}

		return contactManager.getContactType(typeId);
	}

	@RequestMapping(value="/manage-person", params={"deletePerson"}, method=POST)
	public String deleteEmployee(@RequestParam("personId") Long personId, ModelMap map, RedirectAttributes redirectAttributes) {
		String deleteResult = "";

		try{
			int deleted = personManager.deletePerson(personId);
			deleteResult = 	(deleted > 0)?"Successfully deleted person with id = " + personId:"Cannot delete person with id = " + personId;
		} catch(Exception e) {
			deleteResult = "Person with id " + personId + " does not exist";				
		}
		redirectAttributes.addFlashAttribute("deleteResult", deleteResult);

		return "redirect:/home";
	}
}