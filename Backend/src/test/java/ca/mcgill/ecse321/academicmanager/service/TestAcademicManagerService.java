package ca.mcgill.ecse321.academicmanager.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.dom4j.IllegalAddException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.mcgill.ecse321.academicmanager.dao.*;

import ca.mcgill.ecse321.academicmanager.exceptions.*;

import ca.mcgill.ecse321.academicmanager.model.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestAcademicManagerService {

	@Autowired
	private CooperatorService cooperatorService;
	@Autowired
	private CoopTermRegistrationService coopTermRegistrationService;
	@Autowired
	private CourseService courseService;
	@Autowired
	private FormService formService;
	@Autowired
	private MeetingService meetingService;
	@Autowired
	private StudentService studentService;
	@Autowired
	private TermService termService;

	@Autowired
	private CooperatorRepository cooperatorRepository;
	@Autowired
	private CoopTermRegistrationRepository coopTermRegistrationRepository;
	@Autowired
	private CourseRepository courseRepository;
	@Autowired
	private FormRepository formRepository;
	@Autowired
	private MeetingRepository meetingRepository;
	@Autowired
	private StudentRepository studentRepository;
	@Autowired
	private TermRepository termRepository;

	Cooperator cooperator;

	@Before
	public void createCooperator() {
		cooperatorService.create(1);
		Set<Cooperator> allCooperators = cooperatorService.getAll();
		assertEquals(1, allCooperators.size());
		cooperator = allCooperators.iterator().next();
	}

	@After
	public void clearDatabase() {
		courseRepository.deleteAll();
		formRepository.deleteAll();
		coopTermRegistrationRepository.deleteAll();
		termRepository.deleteAll();
		meetingRepository.deleteAll();
		studentRepository.deleteAll();
		cooperatorRepository.deleteAll();
	}

	@Test
	public void testCreateCourse() {
		assertEquals(0, courseService.getAll().size());

		String courseID = "ECSE321";
		String term = "Winter2019";
		String courseName = "Introduction to Software Engineering";
		Integer courseRank = null;

		try {
			courseService.create(courseID, term, courseName, courseRank, cooperator);
		} catch (Exception e) {
			// Check that no error occurred
			fail();
		}

		Set<Course> allCourses = courseService.getAll();

		assertEquals(1, allCourses.size());
		Course tmp = allCourses.iterator().next();
		assertEquals(courseID, tmp.getCourseID());
		assertEquals(term, tmp.getTerm());
	}

	@Test
	public void testCreateCourseNull() {
		assertEquals(0, courseService.getAll().size());

		try {
			courseService.create(null, null, "Nothing", null, cooperator);
		} catch (NullArgumentException e) {
			// assert no changes in memory
			assertEquals(0, courseService.getAll().size());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testUpdateCourseRank() {
		String courseID = "ECSE321";
		String term = "Winter2019";
		String courseName = "Introduction to Software Engineering";
		Integer courseRank = 2;

		Course course = courseService.create(courseID, term, courseName, courseRank, cooperator);
		assertEquals(course.getCourseRank(), courseRank);

		courseRank = 1;
		course = courseService.updateRank(course, courseRank);

		assertEquals(courseRank, course.getCourseRank());
		assertEquals(1, courseService.getAll().size());
	}

	@Test
	public void testUpdateCourseRankNull() {
		String courseID = "ECSE321";
		String term = "Winter2019";
		String courseName = "Introduction to Software Engineering";
		Integer courseRank = 2;

		Course course = courseService.create(courseID, term, courseName, courseRank, cooperator);
		assertEquals(course.getCourseRank(), courseRank);

		course = courseService.updateRank(course, null);

		assertEquals(courseRank, course.getCourseRank());
		assertEquals(1, courseService.getAll().size());
	}

	@Test
	public void testCreateStudent() {
		assertEquals(0, studentService.getAll().size());

		String studentID = "260632353";
		String firstname = "Saleh";
		String lastname = "Bakhit";

		try {
			studentService.create(studentID, firstname, lastname, cooperator);
		} catch (IllegalArgumentException e) {
			fail();
		}

		Set<Student> allStudents = studentService.getAll();

		assertEquals(1, allStudents.size());
		Student tmp = allStudents.iterator().next();
		assertEquals(studentID, tmp.getStudentID());
	}

	@Test
	public void viewAllStudents() {
		String studentID = "1";
		String firstname = "1";
		String lastname = "1";

		studentService.create(studentID, firstname, lastname, cooperator);

		studentID = "2";
		firstname = "2";
		lastname = "2";

		studentService.create(studentID, firstname, lastname, cooperator);

		Set<Student> students = studentService.getAll();

		assertTrue(students.size() == 2);
	}

	@Test
	public void testViewStudentGrade() {
		Student student = studentService.create("142142", "1", "1", cooperator);
		Term term = termService.create("Winter2019", "Winter 2019", null, null);

		String registrationID = "1214214";
		String jobID = "1512521";
		TermStatus status = TermStatus.FAILED;
		Grade grade = Grade.NotGraded;

		CoopTermRegistration ctr = null;
		try {
			ctr = coopTermRegistrationService.create(registrationID, jobID, status, grade, student, term);
		} catch (Exception e) {
			fail();
		}

		assertEquals(grade, studentService.getStudentGrade(ctr));
	}

	@Test
	public void testCreateStudentNull() {
		assertEquals(0, studentService.getAll().size());

		String studentID = null;
		String firstname = null;
		String lasttname = null;

		try {
			studentService.create(studentID, firstname, lasttname, cooperator);
		} catch (NullArgumentException e) {
			assertEquals(0, studentService.getAll().size());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testCreateForm() {
		Student student = studentService.create("142142", "1", "1", cooperator);
		Term term = termService.create("Winter2019", "Winter 2019", null, null);
		CoopTermRegistration ctr = coopTermRegistrationService.create("1214214", "1512521", TermStatus.FAILED, Grade.A,
				student, term);

		String formID = "142142";
		String pdfLink = "1";
		String formName = "1";
		FormType formType = FormType.STUDENTEVALUATION;
		try {
			Form form = formService.create(formID, formName, pdfLink, formType, ctr);
			assertEquals(1, formRepository.count());
			assertEquals("142142", form.getFormID());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testCreateFormNull() {
		try {
			formService.create(null, "nullForm", null, FormType.STUDENTEVALUATION, null);
		} catch (NullArgumentException e) {
			// check no changes in memory
			assertEquals(0, formRepository.count());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testUpdateForm() {
		Student tmpStudent = studentService.create("1", "first", "last", cooperator);
		Term tmpTerm = termService.create("Winter2019", "Winter 2019", null, null);
		CoopTermRegistration tmpCTR = coopTermRegistrationService.create("0", "123", TermStatus.ONGOING, null,
				tmpStudent, tmpTerm);

		String formID = "142142";
		String pdfLink = "1";
		String formName = "1";
		FormType formType = FormType.STUDENTEVALUATION;

		Form form = formService.create(formID, formName, pdfLink, formType, tmpCTR);

		pdfLink = "2";
		formName = "2";
		formType = FormType.COOPEVALUATION;

		form = formService.updateLink(form, pdfLink);
		form = formService.updateName(form, formName);
		form = formService.updateType(form, formType);

		assertEquals(1, formRepository.count());
		assertEquals("142142", form.getFormID());
		assertEquals("2", form.getPdfLink());
		assertEquals("2", form.getName());
		assertEquals(FormType.COOPEVALUATION, form.getFormType());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateNonexistingForm() {
		String pdfLink = "2";
		String formName = "2";
		FormType formType = FormType.COOPEVALUATION;

		formService.updateLink(null, pdfLink);
		formService.updateName(null, formName);
		formService.updateType(null, formType);
	}

	@Test
	public void testUpdateFormNull() {
		Student tmpStudent = studentService.create("1", "first", "last", cooperator);
		Term tmpTerm = termService.create("Winter2019", "Winter 2019", null, null);
		CoopTermRegistration tmpCTR = coopTermRegistrationService.create("0", "123", TermStatus.ONGOING, null,
				tmpStudent, tmpTerm);

		String formID = "142142";
		String pdfLink = "1";
		String formName = "1";
		FormType formType = FormType.STUDENTEVALUATION;

		Form form = formService.create(formID, formName, pdfLink, formType, tmpCTR);

		form = formService.updateLink(form, null);
		form = formService.updateName(form, null);
		form = formService.updateType(form, null);

		assertEquals(pdfLink, form.getPdfLink());
		assertEquals(formName, form.getName());
		assertEquals(formType, form.getFormType());
	}

	@Test
	public void testViewEmployerEvalForms() {
		Student student = studentService.create("142142", "1", "1", cooperator);
		Term term = termService.create("Winter2019", "Winter 2019", null, null);
		CoopTermRegistration ctr = coopTermRegistrationService.create("1214214", "1512521", TermStatus.FAILED, Grade.A,
				student, term);

		Form form = formService.create("142142", "1", "1", FormType.STUDENTEVALUATION, ctr);

		assertEquals(1, formRepository.count());
		assertEquals("142142", form.getFormID());

		Set<Form> forms = coopTermRegistrationService.get("1214214").getForm();

		for (Form f : forms) {
			assertEquals(FormType.STUDENTEVALUATION, f.getFormType());
		}
	}

	@Test
	public void testViewProblematicStudents() {
		String studentID = "142142";
		String firstname = "1";
		String lastname = "1";

		Student student = studentService.create(studentID, firstname, lastname, cooperator);
		studentService.updateProblematicStatus(student, true);

		List<Student> students = studentService.getAllProblematicStudents();

		for (Student s : students) {
			assertEquals(true, s.isIsProblematic());
		}
	}

	@Test
	public void testAdjudicateTermStatus() {
		Student student = studentService.create("142142", "1", "1", cooperator);
		Term term = termService.create("Winter2019", "Winter 2019", null, null);

		CoopTermRegistration ctr = coopTermRegistrationService.create("1214214", "1512521", TermStatus.FAILED, Grade.A,
				student, term);
		assertEquals(TermStatus.FAILED, ctr.getTermStatus());

		coopTermRegistrationService.updateTermStatus(ctr, TermStatus.FINISHED);
		assertEquals(TermStatus.FINISHED, ctr.getTermStatus());
	}

	/**
	 * Test time constraint of the Meeting object.
	 * 
	 * @author ecse321-winter2019-group18
	 * @since 2019-02-10
	 */
	/*
	 * @Test public void testCreateMeetingStartTimeAfterEndTime() { assertEquals(0,
	 * service.getAllMeetings().size()); // list of test instances String meetingID
	 * = "123456"; String location = "sample location"; String details =
	 * "sample details"; Date date = Date.valueOf("2019-01-01"); Time startTime =
	 * Time.valueOf("18:00:00"); Time endTime = Time.valueOf("16:00:00"); try {
	 * service.createMeeting(meetingID, location, details, date, startTime,
	 * endTime); } catch (InvalidEndTimeException e) { assertEquals(0,
	 * service.getAllMeetings().size()); } catch (Exception e) { fail(); } }
	 * 
	 * @Test public void testCreateMeeting() { assertEquals(0,
	 * service.getAllMeetings().size()); // list of test instances String meetingID
	 * = "123456"; String location = "sample location"; String details =
	 * "sample details"; Date date = Date.valueOf("2019-01-01"); Time startTime =
	 * Time.valueOf("16:00:00"); Time endTime = Time.valueOf("18:00:00"); try {
	 * Meeting meeting = service.createMeeting(meetingID, location, details, date,
	 * startTime, endTime); assertEquals(1, meetingRepository.count());
	 * assertEquals(meeting, meetingRepository.findByMeetingID(meetingID)); } catch
	 * (Exception e) { fail(); } }
	 * 
	 *//**
		 * Test case: a null Meeting object
		 * 
		 * @author ecse321-winter2019-group18
		 * @since 2019-02-10
		 *//*
			 * @Test public void testCreateMeetingNull() { assertEquals(0,
			 * service.getAllMeetings().size()); // list of test instances String meetingID
			 * = null; String location = null; String details = null; Date date =
			 * Date.valueOf("2019-01-01"); Time startTime = Time.valueOf("16:00:00"); Time
			 * endTime = Time.valueOf("18:00:00"); try { service.createMeeting(meetingID,
			 * location, details, date, startTime, endTime); } catch (NullArgumentException
			 * e) { assertEquals(0, service.getAllMeetings().size()); } catch (Exception e)
			 * { fail(); } }
			 * 
			 * @Test public void testUpdateMeeting() { String meetingID = "123456"; String
			 * location = "sample location"; String details = "sample details"; Date date =
			 * Date.valueOf("2019-01-01"); Time startTime = Time.valueOf("16:00:00"); Time
			 * endTime = Time.valueOf("18:00:00");
			 * 
			 * Meeting meeting = service.createMeeting(meetingID, location, details, date,
			 * startTime, endTime);
			 * 
			 * location = "new location"; details = "new deatils"; date =
			 * Date.valueOf("2019-01-02"); startTime = Time.valueOf("15:00:00"); endTime =
			 * Time.valueOf("17:00:00");
			 * 
			 * meeting = service.updateMeeting(meeting, location, details, date, startTime,
			 * endTime, null);
			 * 
			 * assertEquals(service.getAllMeetings().size(), 1);
			 * assertEquals(meeting.getLocation(), location);
			 * assertEquals(meeting.getDetails(), details); assertEquals(meeting.getDate(),
			 * date); assertEquals(meeting.getStartTime(), startTime);
			 * assertEquals(meeting.getEndTime(), endTime); }
			 */

	@Test
	public void testCreateCoopTermRegistration() {

		String studentID = "142142";
		String firstname = "1";
		String lastname = "1";

		Student tmpStudent = studentService.create(studentID, firstname, lastname, cooperator);
		Term tmpTerm = termService.create("Winter2019", "Winter 2019", null, null);

		String registrationID = "1214214";
		String jobID = "1512521";
		TermStatus status = TermStatus.FAILED;
		Grade grade = Grade.A;

		try {
			CoopTermRegistration tmpCTR = coopTermRegistrationService.create(registrationID, jobID, status, grade,
					tmpStudent, tmpTerm);
			assertEquals(tmpCTR.getRegistrationID(), registrationID);
			assertEquals(tmpCTR.getJobID(), jobID);
			assertEquals(tmpCTR.getTermStatus(), status);
			assertEquals(tmpCTR.getGrade(), Grade.A);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}

	@Test
	public void testUpdateCoopTermRegistrationsStatus() {

		String studentID = "142142";
		String firstname = "1";
		String lastname = "1";

		Student tmpStudent = studentService.create(studentID, firstname, lastname, cooperator);
		Term tmpTerm = termService.create("Winter2019", "Winter 2019", null, null);

		String registrationID = "1214214";
		String jobID = "1512521";
		TermStatus status = TermStatus.FAILED;
		Grade grade = Grade.A;

		CoopTermRegistration tmpCTR = coopTermRegistrationService.create(registrationID, jobID, status, grade,
				tmpStudent, tmpTerm);

		tmpCTR = coopTermRegistrationService.updateTermStatus(tmpCTR, TermStatus.FINISHED);

		assertEquals(tmpCTR.getTermStatus(), TermStatus.FINISHED);
	}

	@Test
	public void testUpdateCoopTermRegistrationGrade() {
		String studentID = "142142";
		String firstname = "1";
		String lastname = "1";

		Student tmpStudent = studentService.create(studentID, firstname, lastname, cooperator);
		Term tmpTerm = termService.create("Winter2019", "Winter 2019", null, null);

		String registrationID = "1214214";
		String jobID = "1512521";
		TermStatus status = TermStatus.FAILED;
		Grade grade = Grade.A;

		CoopTermRegistration tmpCTR = coopTermRegistrationService.create(registrationID, jobID, status, grade,
				tmpStudent, tmpTerm);

		tmpCTR = coopTermRegistrationService.updateTermGrade(tmpCTR, Grade.B);

		assertEquals(tmpCTR.getGrade(), Grade.B);
	}

	@Test
	public void testUpdateCoopTermRegistrationNull() {

		String studentID = "142142";
		String firstname = "1";
		String lastname = "1";

		Student tmpStudent = studentService.create(studentID, firstname, lastname, cooperator);
		Term tmpTerm = termService.create("Winter2019", "Winter 2019", null, null);

		String registrationID = "1214214";
		String jobID = "1512521";
		TermStatus status = TermStatus.FAILED;
		Grade grade = Grade.A;

		CoopTermRegistration tmpCTR = coopTermRegistrationService.create(registrationID, jobID, status, grade,
				tmpStudent, tmpTerm);

		tmpCTR = coopTermRegistrationService.updateTermGrade(tmpCTR, null);
		tmpCTR = coopTermRegistrationService.updateTermStatus(tmpCTR, null);

		assertEquals(tmpCTR.getTermStatus(), status);
		assertEquals(tmpCTR.getGrade(), grade);
	}

	@Test
	public void testCreateTerm() {
		String termID = "696969";
		String termName = "Winter 2019";
		Date studentEvalFormDeadline = Date.valueOf("2015-06-01");
		Date coopEvalFormDeadline = Date.valueOf("2015-06-01");
		try {
			Term term = termService.create(termID, termName, studentEvalFormDeadline, coopEvalFormDeadline);
			// assertEquals(term.getTermID(), termID);
			// assertEquals(term.getStudentEvalFormDeadline(), studentEvalFormDeadline);
			// assertEquals(term.getCoopEvalFormDeadline(), coopEvalFormDeadline);
			assertEquals(term, termService.get(termID));
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testCreateTermNull() {
		assertEquals(0, termRepository.count());

		try {
			termService.create(null, null, null, null);
		} catch (NullArgumentException e) {
			assertEquals(0, termRepository.count());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testUpdateTerm() {
		Term term;

		String termID = "696969";
		String termName = "Winter2019";
		Date studentEvalFormDeadline = Date.valueOf("2015-06-01");
		Date coopEvalFormDeadline = Date.valueOf("2015-06-01");

		term = termService.create(termID, termName, studentEvalFormDeadline, coopEvalFormDeadline);

		studentEvalFormDeadline = Date.valueOf("2017-06-01");
		coopEvalFormDeadline = Date.valueOf("2017-06-01");
		termName = "Fall2019";

		term = termService.updateCoopEvalDeadline(term, coopEvalFormDeadline);
		term = termService.updateName(term, termName);
		term = termService.updateStudentEvalDeadline(term, studentEvalFormDeadline);

		assertEquals(term.getCoopEvalFormDeadline(), coopEvalFormDeadline);
		assertEquals(term.getStudentEvalFormDeadline(), studentEvalFormDeadline);
		assertEquals(term.getTermName(), termName);
	}

	@Test
	public void testUpdateTermNull() {
		Term term;

		String termID = "696969";
		String termName = "Winter2019";
		Date studentEvalFormDeadline = Date.valueOf("2015-06-01");
		Date coopEvalFormDeadline = Date.valueOf("2015-06-01");

		term = termService.create(termID, termName, studentEvalFormDeadline, coopEvalFormDeadline);

		term = termService.updateCoopEvalDeadline(term, null);
		term = termService.updateName(term, null);
		term = termService.updateStudentEvalDeadline(term, null);

		assertEquals(term.getTermID(), termID);
		assertEquals(term.getTermName(), termName);
		assertEquals(term.getCoopEvalFormDeadline(), coopEvalFormDeadline);
		assertEquals(term.getStudentEvalFormDeadline(), studentEvalFormDeadline);
	}

	@Test(expected = IllegalAddException.class)
	public void testAddTwoInternshipsFail() {
		Student s1 = studentService.create("1", "s1First", "s1Last", cooperator);

		Term t1 = termService.create("1", "1-Term", null, null);

		coopTermRegistrationService.create("1", "1", TermStatus.ONGOING, null, s1, t1);

		// should fail here (same student same term)
		coopTermRegistrationService.create("2", "2", TermStatus.ONGOING, null, studentService.get("1"),
				termService.get("1"));

	}

	@Test
	public void testGetCoopTermRegistrationsByStudentID() {
		String studentID = "142142";
		String firstname = "1";
		String lastname = "1";

		Student tmpStudent = studentService.create(studentID, firstname, lastname, cooperator);
		Term tmpTerm = termService.create("Winter2019", "Winter 2019", null, null);

		String registrationID = "1214214";
		String jobID = "1512521";
		TermStatus status = TermStatus.FAILED;
		Grade grade = Grade.A;

		CoopTermRegistration tmpCTR = coopTermRegistrationService.create(registrationID, jobID, status, grade,
				tmpStudent, tmpTerm);

		Set<CoopTermRegistration> ctr = coopTermRegistrationService.getByStudentID(tmpStudent.getStudentID());

		assertEquals(ctr.size(), 1);
		assertEquals(ctr.iterator().next().getRegistrationID(), "1214214");
	}

}
