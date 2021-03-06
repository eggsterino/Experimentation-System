package com.example.demo.BusinessLayer;

import com.example.demo.BusinessLayer.Entities.Experiment;
import com.example.demo.BusinessLayer.Entities.Grader;
import com.example.demo.BusinessLayer.Entities.GradingTask.GradingTask;
import com.example.demo.BusinessLayer.Entities.ManagementUserToExperiment;
import com.example.demo.BusinessLayer.Entities.Participant;
import com.example.demo.BusinessLayer.Entities.Stages.Stage;
import com.example.demo.BusinessLayer.Exceptions.ExistException;
import com.example.demo.BusinessLayer.Exceptions.FormatException;
import com.example.demo.BusinessLayer.Exceptions.NotExistException;
import com.example.demo.BusinessLayer.Exceptions.NotInReachException;

import java.util.List;
import java.util.Map;

public interface ICreatorBusiness {

    //Login
    boolean researcherLogin(String username, String password);

    int createExperiment(String researcherName, String expName) throws ExistException, NotExistException;

    //UC 1.1 - one choice (PARTS)
    void addStageToExperiment(String researcherName, int id, Map<String, Object> stage) throws ExistException, FormatException, NotExistException;

    //String saveExperiment(String researcherName, int id) throws NotExistException;

    //UC 1.1 - second choice (ALL)
    int addExperiment(String researcherName, String expName, List<Map<String, Object>> stages) throws NotExistException, FormatException, ExistException, NotInReachException;

    //UC 1.2 - one choice (ALL)
    int addGradingTask(String researcherName, int expId, String gradTaskName, List<Map<String, Object>> ExpeeExp,
                       List<Integer> stagesToCheck, List<Map<String, Object>> personalExp) throws NotExistException, FormatException;

    //UC 1.2 - second choice (PARTS)
    // the two funcs below can maybe use addStageToExperiment(String researcherName, String expName/taskId, JSONObject stage)
    void addToPersonal(String researcherName, int expId, int taskId, Map<String, Object> stage) throws NotExistException, FormatException;

    void addToResultsExp(String researcherName, int expId, int taskId, Map<String, Object> stage) throws NotExistException, FormatException;

    void setStagesToCheck(String researcherName, int expId, int taskId, List<Integer> stagesToCheck) throws NotExistException, FormatException;

    //String saveGradingTask(String researcherName, int expId, int taskId) throws NotExistException;

    //UC 1.3
    void setAlliePermissions(String researcherName, int expId, String allieMail, String allieRole, List<String> permissions) throws NotExistException;

    String addGraderToGradingTask(String researcherName, int expId, int taskId, String graderMail) throws NotExistException, ExistException;

    String addExperimentee(String researcherName, int expId, String ExpeeMail) throws NotExistException, ExistException;

    List<String> addExperimentees(String researcherName, int expId, List<String> expeeMails) throws NotExistException, ExistException;

    void addExpeeToGrader(String researcherName, int expId, int taskId, String graderMail, String expeeMail) throws NotExistException, ExistException;

    //meaningful getters

    List<Experiment> getExperiments(String username) throws NotExistException;

    List<Stage> getStages(String username, int expId) throws NotExistException;

    List<Participant> getExperimentees(String username, int expId) throws NotExistException;

    List<ManagementUserToExperiment> getAllies(String username, int expId) throws NotExistException;

    List<GradingTask> getGradingTasks(String username, int expId) throws NotExistException;

    List<Stage> getPersonalStages(String username, int expId, int taskId) throws NotExistException;

    List<Stage> getEvaluationStages(String username, int expId, int taskId) throws NotExistException;

    List<Grader> getTaskGraders(String username, int expId, int taskId) throws NotExistException;

    List<Participant> getTaskExperimentees(String username, int expId, int taskId) throws NotExistException;
}
