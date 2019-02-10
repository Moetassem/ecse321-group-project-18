package ca.mcgill.ecse321.academicmanager.dao;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.academicmanager.model.Form;

public interface FormRepository extends CrudRepository<Form, String> {
	Form findFormByPdfLink(String pdfLink);
	Form findFormByName(String name);
}
