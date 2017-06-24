package it.polito.tdp.gestionale.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import it.polito.tdp.gestionale.db.DidatticaDAO;

public class Model {

	private List<Corso> corsi;
	private List<Studente> studenti;
	private DidatticaDAO dao;
	
	private Map<Integer, Studente> mappaStudenti;
	
	private SimpleGraph<Nodo, DefaultEdge> graph;

	public Model() {
		mappaStudenti= new HashMap<Integer, Studente>();
		graph= new SimpleGraph<>(DefaultEdge.class);
		dao= new DidatticaDAO();
	}
	
	private List<Corso> getTuttiICorsi() {
		if(corsi==null){
			corsi= dao.getTuttiICorsi();
			getTuttiStudenti();
			for(Corso c: corsi){
				dao.getStudentiIscrittiAlCorso(c, mappaStudenti);
			}
		}
		return corsi;
	}

	private List<Studente> getTuttiStudenti() {
		if(studenti==null){
			studenti=dao.getTuttiStudenti();
			for(Studente s: studenti){
				mappaStudenti.put(s.getMatricola(), s);
			}
		}
		return studenti;
	}
	
	public void generaGrafo(){
		studenti= getTuttiStudenti();
		corsi= getTuttiICorsi();
		
		Graphs.addAllVertices(graph, studenti);
		Graphs.addAllVertices(graph, corsi);
		
		for(Corso c: corsi){
			for(Studente s: c.getStudenti()){
				graph.addEdge(c, s);
			}
		}
	}
	
	public List<Integer> getStatCorsi(){
		List<Integer> statCorsi= new ArrayList<Integer>();
		//inizializzo a zero le celle della lista
		for(int i=0; i<corsi.size()+1; i++){
			statCorsi.add(0);
		}
		//aggiorno le statistiche
		for(Studente s: studenti){
			int numeroCorsi= graph.degreeOf(s);
			int counter= statCorsi.get(numeroCorsi);
			counter++;
			statCorsi.set(numeroCorsi, counter);
		}
		
		return statCorsi;
	}
	
	public List<Corso> findMinimalSet(){
		List<Corso> soluzioneParziale= new ArrayList<Corso>();
		List<Corso> soluzioneMigliore= new ArrayList<Corso>();
		
		this.recursive(soluzioneParziale, soluzioneMigliore);
		//il livello non serve perchè la ricorsione termina da sola
		
		return soluzioneMigliore;
	}

	private void recursive(List<Corso> soluzioneParziale, List<Corso> soluzioneMigliore) {
		//creo un set con tutti gli studenti
		Set<Studente> hashSetStudenti= new HashSet<>(getTuttiStudenti());
		//per ogni corso della soluzione parziale sottraggo al set di studenti gli studenti di quel corso
		for(Corso corso: soluzioneParziale){
			hashSetStudenti.removeAll(corso.getStudenti());
		}
		//se il set rimane vuoto vuol dire che i corsi della soluzione parziale raggiungono tutti gli studenti
		if(hashSetStudenti.isEmpty()){
			if(soluzioneMigliore.isEmpty()){
				soluzioneMigliore.addAll(soluzioneParziale);
			}
			//se il numero di corsi del parziale è minore del numero di corsi del migliore abbiamo trovato un nuovo best
			if(soluzioneParziale.size()<soluzioneMigliore.size()){
				soluzioneMigliore.clear();
				soluzioneMigliore.addAll(soluzioneParziale);
			}
			
		}else{
			for(Corso c: getTuttiICorsi()){
				//inserisco il nuovo corso se la soluzione parziale è vuota 
				//oppure se il codice corso di c è maggiore del codice dell'ultimo corso inserito
				if(soluzioneParziale.isEmpty() || c.compareTo(soluzioneParziale.get(soluzioneParziale.size()-1))>0){
					soluzioneParziale.add(c);
					recursive(soluzioneParziale, soluzioneMigliore);
					soluzioneParziale.remove(c);
				}
			}
		}
	}
	
}
