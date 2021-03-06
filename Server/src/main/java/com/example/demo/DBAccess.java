package com.example.demo;

import com.example.demo.BusinessLayer.DataCache;
import com.example.demo.BusinessLayer.Entities.*;
import com.example.demo.BusinessLayer.Entities.GradingTask.GraderToGradingTask;
import com.example.demo.BusinessLayer.Entities.GradingTask.GraderToParticipant;
import com.example.demo.BusinessLayer.Entities.GradingTask.GradingTask;
import com.example.demo.BusinessLayer.Entities.Results.*;
import com.example.demo.BusinessLayer.Entities.Stages.*;
import com.example.demo.DataAccessLayer.Reps.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DBAccess {
    @Autowired
    ExperimenteeRep experimenteeRep;
    @Autowired
    ExperimentRep experimentRep;
    @Autowired
    GraderRep graderRep;
    @Autowired
    ManagementUserRep managementUserRep;
    @Autowired
    ParticipantRep participantRep;
    @Autowired
    PermissionRep permissionRep;
    @Autowired
    TaggingStageRep taggingStageRep;
    @Autowired
    StageRep stageRep;
    @Autowired
    QuestionRep questionRep;
    @Autowired
    AnswerRep answerRep;
    @Autowired
    CodeResultRep codeResultRep;
    @Autowired
    RequirementRep requirementRep;
    @Autowired
    RequirementTagRep requirementTagRep;
    @Autowired
    GradingTaskRep gradingTaskRep;
    @Autowired
    GraderToGradingTaskRep graderToGradingTaskRep;
    @Autowired
    GraderToParticipantRep graderToParticipantRep;
    @Autowired
    ManagementUserToExperimentRep managementUserToExperimentRep;
    @Autowired
    ResultRep resultRep;
    @Autowired
    DataCache cache;

    public void deleteData() {
        JpaRepository[] reps = {
                answerRep,
                questionRep,
                codeResultRep,
                requirementTagRep,
                requirementRep,
                graderToParticipantRep,
                graderToGradingTaskRep,
                gradingTaskRep,
                managementUserToExperimentRep,
                taggingStageRep,
                stageRep,
                experimentRep,
                permissionRep,
                managementUserRep,
                permissionRep,
                participantRep,
                experimenteeRep,
                graderRep,
        };
        for (JpaRepository rep : reps)
            rep.deleteAll();
    }

    public void saveExperimentee(Experimentee e) {
        participantRep.save(e.getParticipant());
        experimenteeRep.save(e);
    }

    public List<Experimentee> getAllExperimentees() {
        return experimenteeRep.findAll();
    }

    public long getNumberOfExperimentees() {
        return experimenteeRep.count();
    }

    public Experimentee getExperimenteeByCode(UUID code) {
        return experimenteeRep.findById(code).orElse(null);
    }

    public Experimentee getExperimenteeByEmail(String email) {
        return experimenteeRep.findByEmail(email);
    }

    public Experimentee getExperimenteeByEmailAndExp(String email, int expId) {
        return experimenteeRep.findByEmailAndExp(email, expId);
    }

    public Experimentee getExperimenteeByParticipantId(int partidipantId) {
        return experimenteeRep.findExperimenteeByParticipantId(partidipantId);
    }

    public void saveExperiment(Experiment e, ManagementUser creator) {
        experimentRep.save(e);
        for (ManagementUserToExperiment m : creator.getManagementUserToExperiments()) {
            if (m.getExperiment().getExperimentId() == e.getExperimentId()) {
                managementUserToExperimentRep.save(m);
            }
        }
        managementUserRep.save(creator);
        //cache.updateManagementUser(creator);
    }

    public long getNumberOfManagers() {
        return managementUserRep.count();
    }

    public long getNumberOfExperiments() {
        return experimentRep.count();
    }

    public void deleteExperiment(Experiment e) {
        experimentRep.deleteById(e.getExperimentId());
    }

    public Experiment getExperimentById(int expId) {
        return experimentRep.findById(expId).orElse(null);
    }

    public void saveGrader(Grader g) {
        graderRep.save(g);
    }

    public Grader getGraderByEmail(String email) {
        return graderRep.findById(email).orElse(null);
    }

    public void saveManagementUser(ManagementUser m) {
        managementUserRep.save(m);
    }

    public void deleteManagementUser(ManagementUser m) {
        managementUserRep.delete(m);
    }

    public List<ManagementUser> getAllManagementUsers() {
        return managementUserRep.findAll();
    }

    public ManagementUser getManagementUserByName(String name) {
        return managementUserRep.findById(name).orElse(null);
    }

    public ManagementUser getManagementUserByEMail(String email) {
        return managementUserRep.findByEmail(email);
    }

    public void saveParticipant(Participant p) {
        participantRep.save(p);
    }

    public Participant getParticipantById(int pId) {
        return participantRep.findById(pId).orElse(null);
    }

    public void savePermissionForManagementUser(Permission p, ManagementUser m) {
        permissionRep.save(p);
        managementUserRep.save(m);
    }

    public long getNumberOfPermissions() {
        return permissionRep.count();
    }

    public void deletePermissionsOfManagementUser(ManagementUser m) {
        for (Permission p : m.getPermissions())
            permissionRep.delete(p);
        m.setPermissions(new ArrayList<>());
        managementUserRep.save(m);
    }

    public void saveStage(Stage s) {
        if (s instanceof QuestionnaireStage) {
            List<Question> temp = ((QuestionnaireStage) s).getQuestions();
            ((QuestionnaireStage) s).setQuestions(new ArrayList<>());
            stageRep.save(s);
            for (Question q : temp) {
                questionRep.save(q);
            }
            ((QuestionnaireStage) s).setQuestions(temp);
        }
        if (s instanceof CodeStage) {
            List<Requirement> temp = ((CodeStage) s).getRequirements();
            ((CodeStage) s).setRequirements(new ArrayList<>());
            stageRep.save(s);
            for (Requirement r : temp) {
                requirementRep.save(r);
            }
            ((CodeStage) s).setRequirements(temp);
        }
        stageRep.save(s);
        experimentRep.save(s.getExperiment());
    }

    public void saveStageResult(TaggingResult result) {
        List<RequirementTag> temp1 = result.getTags();
        result.setTags(new ArrayList<>());
        saveResult(result);
        for (RequirementTag tag : temp1) {
            saveRequirementTag(tag);
        }
        result.setTags(temp1);
    }

    public void saveStageResult(QuestionnaireResult result) {
        List<Answer> temp2 = result.getAnswers();
        result.setAnswers(new ArrayList<>());
        saveResult(result);
        for (Answer ans : temp2) {
            saveAnswer(ans);
        }
        result.setAnswers(temp2);
    }

    public void saveStageResult(Result result) {
        Participant p = result.getParticipant();
        if (result instanceof TaggingResult)
            saveStageResult((TaggingResult) result);
        else if (result instanceof QuestionnaireResult)
            saveStageResult((QuestionnaireResult) result);

        saveResult(result);
        saveParticipant(p);
    }

    public long getNumberOfStages() {
        return stageRep.count();
    }

    public void saveQuestion(Question q) {
        questionRep.save(q);
    }

    public void saveAnswer(Answer a) {
        answerRep.save(a);
    }

    public long getNumerOfAnswers() {
        return answerRep.count();
    }

    public void saveCodeResult(CodeResult cr) {
        codeResultRep.save(cr);
    }

    public long getNumerOfCodeResults() {
        return codeResultRep.count();
    }

    public long getNumberOfTagResults() {
        return requirementTagRep.count();
    }

    public void saveRequirement(Requirement r) {
        requirementRep.save(r);
    }

    public void saveRequirementTag(RequirementTag rt) {
        requirementTagRep.save(rt);
    }

    public void saveGradingTask(GradingTask gt) {
        gradingTaskRep.save(gt);
    }

    public GradingTask getGradingTaskById(int gtId) {
        return gradingTaskRep.findById(gtId).orElse(null);
    }

    public List<GradingTask> getAllGradingTasks() {
        return gradingTaskRep.findAll();
    }

    public long getNumberOfGradingTasks() {
        return gradingTaskRep.count();
    }

    public void saveGraderToGradingTask(GraderToGradingTask g) {
        participantRep.save(g.getGeneralExpParticipant());
        graderToGradingTaskRep.save(g);
        graderRep.save(g.getGrader());
        gradingTaskRep.save(g.getGradingTask());
//        cache.updateGrader(g.getGrader());
//        cache.updateGradingTask(g.getGradingTask());
    }

    public long getGraderToGradingTaskCount() {
        return graderToGradingTaskRep.count();
    }

    public GraderToGradingTask getGraderToGradingTaskById(int gtId, String graderEmail) {
        return graderToGradingTaskRep.findById(new GraderToGradingTask.GraderToGradingTaskID(gtId, graderEmail)).orElse(null);
    }

    public GraderToGradingTask getGraderToGradingTaskByCode(UUID code) {
        return graderToGradingTaskRep.findByGradersCode(code);
    }

    public void saveGraderToParticipant(GraderToParticipant g) {
        participantRep.save(g.getGraderParticipant());
        graderToParticipantRep.save(g);
        graderToGradingTaskRep.save(g.getGraderToGradingTask());
        participantRep.save(g.getExpeeParticipant());
    }

    public GraderToParticipant getGraderToParticipantById(int gtId, String graderEmail, int pId) {
        return graderToParticipantRep.findById(new GraderToParticipant.GraderToParticipantID(gtId, graderEmail, pId)).orElse(null);
    }

    public void saveManagementUserToExperiment(ManagementUserToExperiment m) {
        managementUserToExperimentRep.save(m);
    }

    public void saveResult(Result r) {
        resultRep.save(r);
    }
}
