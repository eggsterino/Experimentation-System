package com.example.demo.BusinessLayer;

import com.example.demo.BusinessLayer.Entities.*;
import com.example.demo.BusinessLayer.Entities.GradingTask.GraderToGradingTask;
import com.example.demo.BusinessLayer.Entities.GradingTask.GraderToParticipant;
import com.example.demo.BusinessLayer.Entities.GradingTask.GradingTask;
import com.example.demo.BusinessLayer.Entities.Stages.Stage;
import com.example.demo.BusinessLayer.Exceptions.ExistException;
import com.example.demo.BusinessLayer.Exceptions.FormatException;
import com.example.demo.BusinessLayer.Exceptions.NotExistException;
import com.example.demo.BusinessLayer.Exceptions.NotInReachException;
import com.example.demo.DBAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CreatorBusiness implements ICreatorBusiness {

    private DBAccess db;

    private DataCache cache;

    @Autowired
    public CreatorBusiness(DBAccess db, DataCache cache) {
        this.db = db;
        this.cache = cache;
    }

    @Override
    public boolean researcherLogin(String username, String password) {
        ManagementUser c;
        try {
            c = cache.getManagerByName(username);
        } catch (NotExistException ignore) {
            return false;
        }
        return c.getBguPassword().equals(password);
    }

    @Override
    public int createExperiment(String researcherName, String expName) throws NotExistException, ExistException {
        ManagementUser c = cache.getManagerByName(researcherName);
        try {
            c.getExperimentByName(expName);
            throw new ExistException(expName);
        } catch (NotExistException ignore) {
        }

        Experiment exp = new Experiment(expName, c);
        db.saveExperiment(exp, c);
        return exp.getExperimentId();
    }

    @Override
    public void addStageToExperiment(String researcherName, int id, Map<String, Object> stage) throws FormatException, NotExistException {
        ManagementUser c = cache.getManagerByName(researcherName);
        Experiment exp = c.getExperiment(id);
        Stage toAdd = Stage.parseStage(stage, exp);
        exp.addStage(toAdd);
        db.saveStage(toAdd);
    }

    @Override
    public int addExperiment(String researcherName, String expName, List<Map<String, Object>> stages) throws NotExistException, FormatException, ExistException, NotInReachException {
        ManagementUser c = cache.getManagerByName(researcherName);
        if (!c.isAdmin()) throw new NotInReachException("creating new experiment", "you don't have the permissions");
        try {
            c.getExperimentByName(expName);
            throw new ExistException(expName);
        } catch (NotExistException ignore) {
        }
        Experiment exp = buildExperiment(stages, expName, c);
        return exp.getExperimentId();
    }

    @Override
    public int addGradingTask(String researcherName, int expId, String gradTaskName, List<Map<String, Object>> ExpeeExp, List<Integer> stagesToCheck, List<Map<String, Object>> personalExp) throws NotExistException, FormatException {
        ManagementUser c = cache.getManagerByName(researcherName);
        Experiment exp = c.getExperiment(expId);
        Experiment personal = buildExperiment(personalExp, gradTaskName + "/personal", c);
        Experiment forExpee = buildExperiment(ExpeeExp, gradTaskName + "/forExpee", c);
        personal.setGradingTaskExp(true);
        forExpee.setGradingTaskExp(true);
        GradingTask gt = new GradingTask(gradTaskName, exp, personal, forExpee);
        gt.setStagesByIdx(stagesToCheck);
        cache.addGradingTask(gt);
        return gt.getGradingTaskId();
    }

    @Override
    public void addToPersonal(String researcherName, int expId, int taskId, Map<String, Object> stage) throws NotExistException, FormatException {
        GradingTask gt = cache.getGradingTaskById(researcherName, expId, taskId);
        Experiment personal = gt.getGeneralExperiment();
        Stage toAdd = Stage.parseStage(stage, personal);
        personal.addStage(toAdd);
        db.saveStage(toAdd);
    }

    @Override
    public void addToResultsExp(String researcherName, int expId, int taskId, Map<String, Object> stage) throws NotExistException, FormatException {
        GradingTask gt = cache.getGradingTaskById(researcherName, expId, taskId);
        Experiment resExp = gt.getGradingExperiment();
        Stage toAdd = Stage.parseStage(stage, resExp);
        resExp.addStage(toAdd);
        db.saveStage(toAdd);
    }

    @Override
    public void setStagesToCheck(String researcherName, int expId, int taskId, List<Integer> stagesToCheck) throws NotExistException, FormatException {
        GradingTask gt = cache.getGradingTaskById(researcherName, expId, taskId);
        gt.setStagesByIdx(stagesToCheck);
        db.saveGradingTask(gt);
    }

    public void addAlly(String researcherName, String allyMail, List<String> permissions) throws NotExistException, ExistException {
        // When adding a new ally, his password is TEMP and username is his mail
        cache.getManagerByName(researcherName);
        try {
            cache.getManagerByEMail(allyMail);
            throw new ExistException(allyMail);
        } catch (NotExistException ignore) {
        }
        ManagementUser ally = new ManagementUser(allyMail, "TEMP", allyMail);
        cache.addManager(ally);
        for (String per : permissions) {
            Permission toAdd = new Permission(per);
            ally.addPermission(toAdd);
            db.savePermissionForManagementUser(toAdd, ally);
        }
    }

    public void addAlly(String researcherName, String allyMail, String password) throws NotExistException, ExistException {
        // When adding a new ally, his password is TEMP and username is his mail
        cache.getManagerByName(researcherName);
        try {
            cache.getManagerByEMail(allyMail);
            throw new ExistException(allyMail);
        } catch (NotExistException ignore) {
        }
        ManagementUser ally = createManager(allyMail, password);
        cache.addManager(ally);
    }

    public void addAllyToExperiment(String researcherName, String expName, String allyMail, String password) throws NotExistException, ExistException {
        ManagementUser manager = cache.getManagerByName(researcherName);
        Experiment experiment = manager.getExperimentByName(expName);
        try {
            cache.getManagerByEMail(allyMail);
            throw new ExistException(allyMail);
        } catch (NotExistException ignore) {
        }
        ManagementUser ally = createManager(allyMail, password);
        ManagementUserToExperiment m = new ManagementUserToExperiment(ally, experiment, "");
        ally.addManagementUserToExperiment(m);
        experiment.addManagementUserToExperiment(m);
        db.saveManagementUserToExperiment(m);
        db.deletePermissionsOfManagementUser(ally);
        cache.addManager(ally);
        ally.setPermissions(List.of(new Permission("noAdmin")));
    }

    private ManagementUser createManager(String allyMail, String password) {
        String username = allyMail;
        if (username.contains("@"))
            username = username.substring(0, username.indexOf('@'));
        return new ManagementUser(username, password, allyMail);
    }

    @Override
    public void setAlliePermissions(String researcherName, int expId, String allieMail, String allieRole, List<String> permissions) throws NotExistException {
        ManagementUser c = cache.getManagerByName(researcherName);
        Experiment exp = c.getExperiment(expId);

        ManagementUser ally;
        try {
            ally = cache.getManagerByEMail(allieMail);
        } catch (NotExistException ignore) {
            throw new NotExistException("set allie permissions", "allie not found");
        }
        ManagementUserToExperiment m = new ManagementUserToExperiment(ally, exp, allieRole);
        ally.addManagementUserToExperiment(m);
        exp.addManagementUserToExperiment(m);
        db.saveManagementUserToExperiment(m);
        db.deletePermissionsOfManagementUser(ally);
        for (String per : permissions) {
            Permission toAdd = new Permission(per);
            ally.addPermission(toAdd);
            db.savePermissionForManagementUser(toAdd, ally);
        }
    }

    @Override
    public String addGraderToGradingTask(String researcherName, int expId, int taskId, String graderMail) throws NotExistException, ExistException {
        GradingTask gt = cache.getGradingTaskById(researcherName, expId, taskId);
        Grader grader;
        try {
            grader = cache.getGraderByEMail(graderMail);
        } catch (NotExistException ignore) {
            grader = new Grader(graderMail);
            cache.addGrader(grader);
        }
        return cache.addGraderToGradingTask(gt, grader).toString();
    }

    @Override
    public String addExperimentee(String researcherName, int expId, String expeeMail) throws NotExistException, ExistException {
        ManagementUser c = cache.getManagerByName(researcherName);
        Experiment exp = c.getExperiment(expId);
        if (cache.isExpeeInExperiment(expeeMail, expId))
            throw new ExistException(expeeMail, "experiment " + expId);

        return addExperimentee(exp, expeeMail);
    }

    @Override
    public List<String> addExperimentees(String researcherName, int expId, List<String> expeeMails) throws NotExistException, ExistException {
        ManagementUser c = cache.getManagerByName(researcherName);
        Experiment exp = c.getExperiment(expId);

        validateMails(exp, expeeMails);

        List<String> codes = new ArrayList<>();
        for (String mail : expeeMails) {
            codes.add(addExperimentee(exp, mail));
        }
        return codes;
    }

    public List<String> addExperimentees(String researcherName, String expName, List<String> expeeMails) throws NotExistException, ExistException {
        ManagementUser c = cache.getManagerByName(researcherName);
        Experiment exp = c.getExperimentByName(expName);

        validateMails(exp, expeeMails);

        List<String> codes = new ArrayList<>();
        for (String mail : expeeMails) {
            codes.add(addExperimentee(exp, mail));
        }
        return codes;
    }

    private void validateMails(Experiment experiment, List<String> mails) throws ExistException {
        Map<String, Boolean> mailsMap = new HashMap<>();
        for (String mail : mails) {
            if (mailsMap.get(mail) != null) throw new ExistException(mail, "mail list");
            else mailsMap.put(mail, true);
            if (cache.isExpeeInExperiment(mail, experiment.getExperimentId()))
                throw new ExistException(mail, "experiment " + experiment.getExperimentId());
        }
    }

    private String addExperimentee(Experiment experiment, String mail) {
        Experimentee expee = new Experimentee(mail, experiment);
        cache.addExperimentee(expee);
        return expee.getAccessCode().toString();
    }

    @Override
    public void addExpeeToGrader(String researcherName, int expId, int taskId, String graderMail, String expeeMail) throws NotExistException, ExistException {
        Grader grader = cache.getGraderByEMail(graderMail);
        GradingTask gt = cache.getGradingTaskById(researcherName, expId, taskId);
        Participant participant = cache.getExpeeByMailAndExp(expeeMail, expId).getParticipant();
        GraderToParticipant g = cache.getGraderToParticipants(cache.getGraderToGradingTask(grader, gt), participant);
        if (g != null) { //this participant is already in the graderToGraderTask
            throw new ExistException("user with id " + participant.getParticipantId(), graderMail + " participants");
        } else {
            g = new GraderToParticipant(cache.getGraderToGradingTask(grader, gt), participant);
        }
        cache.addExpeeToGradingTask(gt, grader, g);
        //TODO: fix?
    }

    //meaningful getters

    public List<Experiment> getExperiments(String username) throws NotExistException {
        ManagementUser manager = cache.getManagerByName(username);
        List<Experiment> experiments = manager.getExperimentes();
        List<Experiment> experimentsToRes = new ArrayList<>();
        for (Experiment exp : experiments) {
            if (!exp.isGradingTaskExp()) experimentsToRes.add(exp);
        }
        return experimentsToRes;
    }

    public List<Stage> getStages(String username, int expId) throws NotExistException {
        ManagementUser c = cache.getManagerByName(username);
        Experiment exp = c.getExperiment(expId);
        return exp.getStages();
    }

    public List<Participant> getExperimentees(String username, int expId) throws NotExistException {
        ManagementUser c = cache.getManagerByName(username);
        Experiment exp = c.getExperiment(expId);
        return exp.getParticipants();
    }

    public List<Experimentee> getExperimentees(String username, String expName) throws NotExistException {
        ManagementUser c = cache.getManagerByName(username);
        Experiment exp = c.getExperimentByName(expName);
        List<Participant> participants = exp.getParticipants();
        List<Experimentee> experimentees = new LinkedList<>();
        for (Participant participant : participants) {
            experimentees.add(cache.getExperimenteeById(participant.getParticipantId()));
        }
        return experimentees;
    }

    public List<ManagementUserToExperiment> getAllies(String username, int expId) throws NotExistException {
        ManagementUser c = cache.getManagerByName(username);
        Experiment exp = c.getExperiment(expId);
        return exp.getManagementUserToExperiments();
    }

    public List<GradingTask> getGradingTasks(String username, int expId) throws NotExistException {
        return cache.getAllGradingTasks(username, expId);
    }

    @Override
    public List<Stage> getPersonalStages(String username, int expId, int taskId) throws NotExistException {
        GradingTask gt = cache.getGradingTaskById(username, expId, taskId);
        return gt.getGeneralExperiment().getStages();
    }

    @Override
    public List<Stage> getEvaluationStages(String username, int expId, int taskId) throws NotExistException {
        GradingTask gt = cache.getGradingTaskById(username, expId, taskId);
        return gt.getGradingExperiment().getStages();
    }

    @Override
    public List<Grader> getTaskGraders(String username, int expId, int taskId) throws NotExistException {
        GradingTask gt = cache.getGradingTaskById(username, expId, taskId);
        List<GraderToGradingTask> assignedGradingTasks = gt.getAssignedGradingTasks();
        List<Grader> graders = new ArrayList<>();
        for (GraderToGradingTask graderToTask : assignedGradingTasks) {
            graders.add(graderToTask.getGrader());
        }
        return graders;
    }

    @Override
    public List<Participant> getTaskExperimentees(String username, int expId, int taskId) throws NotExistException {
        GradingTask gt = cache.getGradingTaskById(username, expId, taskId);
        List<GraderToGradingTask> assignedGradingTasks = gt.getAssignedGradingTasks();
        List<Participant> experimentees = new ArrayList<>();
        for (GraderToGradingTask graderToTask : assignedGradingTasks) {
            experimentees.addAll(graderToTask.getParticipants());
        }
        return experimentees;
    }

    // utils
    private Experiment buildExperiment(List<Map<String, Object>> stages, String expName, ManagementUser creator) throws FormatException {
        Experiment exp = new Experiment(expName, creator);
        db.saveExperiment(exp, creator);
        for (Map<String, Object> jStage : stages) {
            try {
                Stage toAdd = Stage.parseStage(jStage, exp);
                exp.addStage(toAdd);
                db.saveStage(toAdd);
            } catch (FormatException e) {
                creator.removeManagementUserToExperimentById(exp);
                db.deleteExperiment(exp);
                throw e;
            }
        }
        return exp;
    }
}
