package com.big.dreamer.doccentral.document.marriage.template;

import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarriageTemplateRepository extends EditableTemplateRepository {

    public MarriageTemplateRepository(ApplicationDirectories directories) {
        super(directories.templatesDirectory("marriage"), List.of(
                new Definition("party.txt", "Datos del contrayente", "Compartido", "",
                        ":name, de :age años de edad, :job, :nationality, :familyStatus, originario de :birthPlace y del domicilio de :settlement, departamento de :state, a quien identifico por medio de :identityType número :document, hijo de :parents:languageAssistance"),
                new Definition("premarital.txt", "Acta prematrimonial", "Expediente prematrimonial", "",
                        "ACTA PREMATRIMONIAL. En :premaritalPlace, departamento de :premaritalState, a las :premaritalTime de :premaritalDate. Ante mí, :notary, :notaryTitle, comparecen :partyOne; y :partyTwo; y BAJO JURAMENTO ME DICEN: I) Que es su intención contraer matrimonio entre sí, que no tienen impedimentos legales ni están sujetos a prohibición alguna. II) :propertyRegime. III) :marriedName. :children :capitulations :proxy Después de cerciorarme de su capacidad legal y de explicar los artículos aplicables del Código de Familia, de común acuerdo se señala la celebración en :celebrationPlace, departamento de :celebrationState, a las :celebrationTime de :celebrationDate. DOY FE de tener a la vista: :documents. Los documentos que corresponden quedan incorporados al expediente matrimonial. Leída íntegramente esta acta, ratifican su contenido y firmamos. DOY FE."),
                new Definition("marriage.txt", "Escritura matriz", "Celebración", "",
                        "NÚMERO :deedNumber. MATRIMONIO. En :celebrationPlace, departamento de :celebrationState, a las :celebrationTime de :celebrationDate. Ante mí, :notary, :notaryTitle, por ser este el lugar, día y hora señalados en el acta prematrimonial otorgada a las :premaritalTime de :premaritalDate, comparecen :partyOne; y :partyTwo; y ante los testigos hábiles :witnesses, quienes manifiestan que conocen a los contrayentes. Hice saber el objeto de la reunión, la igualdad de derechos y deberes de los cónyuges, sus responsabilidades para con los hijos y la conservación de la unidad de la familia; di lectura y explicación a las disposiciones aplicables del Código de Familia. Pregunté separadamente a cada contrayente si desea unirse en matrimonio con el otro y ambos contestaron: «SÍ, QUIERO». EN NOMBRE DE LA REPÚBLICA, QUEDAN UNIDOS SOLEMNEMENTE EN MATRIMONIO Y ESTÁN OBLIGADOS A GUARDARSE FIDELIDAD Y ASISTIRSE MUTUAMENTE EN TODAS LAS CIRCUNSTANCIAS DE LA VIDA. Hago constar que comprobé su capacidad legal y la inexistencia de prohibiciones. :propertyRegime. :marriedName. :children :capitulations :proxy Agregaré al legajo de anexos de mi protocolo los documentos del expediente matrimonial. Leído íntegramente este instrumento ante los testigos, ratifican su contenido y firmamos. DOY FE."),
                new Definition("registration.txt", "Control registral", "Actuaciones posteriores", "No forma parte de la escritura matriz.",
                        "HOJA DE CONTROL PARA INSCRIPCIÓN. Matrimonio de :partyOneName y :partyTwoName, celebrado el :celebrationDate en :celebrationPlace, departamento de :celebrationState, bajo el régimen de :propertyRegimeName. Notario autorizante: :notary. :capitulationsRegistration :childrenRegistration ACTUACIONES: :postActions")));
    }
}
