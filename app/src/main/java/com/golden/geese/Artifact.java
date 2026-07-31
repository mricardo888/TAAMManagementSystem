/*
 * Artifact
 * Version 1.0
 * Bob Zhao July 14, 2026
 *
 * This code is provided as part of the coursework for CSCB07H3
 * at the University of Toronto.
 *
 * Unauthorized reproduction, distribution, or sharing of this code is strictly
 * prohibited and constitutes a violation of the University of
 * Toronto Code of Behaviour on Academic Matters.
 *
 */
package com.golden.geese;

import java.util.*; // imports needed if image platform changes

public class Artifact{
    private int lotNum;
    private String name;
    private String description;
    private String category;
    private String[] materials;
    private String dynasty;
    private String origin;
    private double[] dimensions;
    private String conditionReport;
    private String location;
    private String acqMethod;
    private String provenance;
    private int accessionNum;
    private String notes;
    private String image;
    public int likes;
    public List<Comment> comments;
    public int saves;


    /**
     * Primary Constructor
     * Provides null values for all fields in an Artifact object
     */
    public Artifact(){
        lotNum = 0;
        name = "";
        description = "";
        category = "";
        materials = new String[0];
        dynasty = "";
        origin = "";
        dimensions = new double[3];
        conditionReport = "";
        location = "";
        acqMethod = "";
        provenance = "";
        accessionNum = 0;
        notes = "";
        image = "";
        likes = 0;
        comments = new ArrayList<>();
        saves = 0;
    }

    /**
     * Secondary Constructor - adds all mandatory parameters of an artifacts
     * @param lotNum - the lot number integer
     * @param name - name of the artifact in String
     * @param description - Description of the artifact
     * @param category - category the artifact is in
     * @param materials - String array of materials in the artifact
     * @param dynasty - String dynasty when the artifact is from
     */
    public Artifact(int lotNum, String name, String description, String category,
                    String[] materials, String dynasty){
        this();
        this.lotNum = lotNum;
        this.name = name;
        this.description = description;
        this.category = category;
        this.materials = materials;
        this.dynasty = dynasty;
    }

    /**
     * Tertiary Constructor
     * @param lotNum
     * @param name
     * @param description
     * @param category
     * @param materials
     * @param dynasty
     * @param origin
     * @param dimensions
     * @param conditionReport
     * @param location
     * @param acqMethod
     * @param provenance
     * @param accessionNum
     * @param notes
     * @param image
     */
    public Artifact(int lotNum, String name, String description, String category,
                    String[] materials, String dynasty, String origin, double[] dimensions,
                    String conditionReport, String location, String acqMethod, String provenance,
                    int accessionNum, String notes, String image){
        this(lotNum, name, description, category, materials, dynasty);
        this.origin = origin;
        this.dimensions = dimensions;
        this.conditionReport = conditionReport;
        this.location = location;
        this.acqMethod = acqMethod;
        this.provenance = provenance;
        this.accessionNum = accessionNum;
        this.notes = notes;
        this.image = image;
    }

    /**
     * Getter for the lot number
     * @return - the lot number integer
     */
    public int getLotNum() {
        return lotNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String[] getMaterials() {
        return materials;
    }

    public void setMaterials(String[] materials) {
        this.materials = materials;
    }

    public String getDynasty() {
        return dynasty;
    }

    public void setDynasty(String dynasty) {
        this.dynasty = dynasty;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public double[] getDimensions() {
        return dimensions;
    }

    public void setDimensions(double[] dimensions) {
        this.dimensions = dimensions;
    }

    public String getConditionReport() {
        return conditionReport;
    }

    public void setConditionReport(String conditionReport) {
        this.conditionReport = conditionReport;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAcqMethod() {
        return acqMethod;
    }

    public void setAcqMethod(String acqMethod) {
        this.acqMethod = acqMethod;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public int getAccessionNum() {
        return accessionNum;
    }

    public void setAccessionNum(int accessionNum) {
        this.accessionNum = accessionNum;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getSaves() {
        return saves;
    }

    public void setSaves(int saves) {
        this.saves = saves;
    }

    /**
     * toString Method
     * @return - all details about the object artifact
     */
    @Override
    public String toString() {
        return "Artifact{" +
                "lotNum=" + lotNum +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", materials=" + Arrays.toString(materials) +
                ", dynasty='" + dynasty + '\'' +
                ", origin='" + origin + '\'' +
                ", dimensions=" + Arrays.toString(dimensions) +
                ", conditionReport='" + conditionReport + '\'' +
                ", location='" + location + '\'' +
                ", acqMethod='" + acqMethod + '\'' +
                ", provenance='" + provenance + '\'' +
                ", accessionNum=" + accessionNum +
                ", notes='" + notes + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    /**
     * Equals method
     * @param o - another object to compare with
     * @return - boolean true or false dependent on the lot number and the
     * name of the object Artifact
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Artifact artifact)) return false;
        return lotNum == artifact.lotNum && Objects.equals(name, artifact.name);
    }

    /**
     * hashcode method
     * @return - hashed of lotNum and name; can change later
     */
    @Override
    public int hashCode() {
        return Objects.hash(lotNum, name);
    }
}
