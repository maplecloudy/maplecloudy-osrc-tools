package com.maplecloudy.osrc.model.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.maplecloudy.osrc.model.maven.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "OSRC上的应用ApplicationManifest")
public abstract class AbstractApp extends AppLocation {

  @Schema(description = "应用的名称")
  private String name;

  @Schema(description = "应用的类型")
  private String subType;

  @Schema(description = "应用的简介，可以是Markdown形式的文档")
  private String description;

  /**
   * Declares to which version of project descriptor this POM
   * conforms.
   */
  private String modelVersion;

  /**
   * The location of the parent project, if one exists. Values
   * from the parent
   *             project will be the default for this project if
   * they are left unspecified. The location
   *             is given as a group ID, artifact ID and version.
   */
  private Parent parent;

  /**
   *
   *
   *             The type of artifact this project produces, for
   * example <code>jar</code>
   *               <code>war</code>
   *               <code>ear</code>
   *               <code>pom</code>.
   *             Plugins can create their own packaging, and
   *             therefore their own packaging types,
   *             so this list does not contain all possible
   * types.
   *
   *
   */
  private String packaging = "jar";

  /**
   *
   *
   *             The URL to the project's homepage.
   *             <br><b>Default value is</b>: parent value [+
   * path adjustment] + (artifactId or
   * <code>project.directory</code> property), or just parent
   * value if
   *
   * <code>child.urls.inherit.append.path="false"</code>
   *
   *
   */
  private String url;

  /**
   *
   *
   *             When childs inherit from urls, append path or
   * not?. Note: While the type
   *             of this field is <code>String</code> for
   * technical reasons, the semantic type is actually
   *             <code>Boolean</code>
   *             <br /><b>Default value is</b>: <code>true</code>
   *
   *
   */
  private String childInheritAppendPath;

  /**
   * The year of the project's inception, specified with 4
   * digits. This value is
   *             used when generating copyright notices as well
   * as being informational.
   */
  private String inceptionYear;

  /**
   * This element describes various attributes of the
   * organization to which the
   *             project belongs. These attributes are utilized
   * when documentation is created (for
   *             copyright notices and links).
   */
  private Organization organization;

  /**
   * Field licenses.
   */
  private java.util.List<License> licenses;

  /**
   * Field developers.
   */
  private java.util.List<Developer> developers;

  /**
   * Field contributors.
   */
  private java.util.List<Contributor> contributors;

  /**
   * Field mailingLists.
   */
  private java.util.List<MailingList> mailingLists;

  /**
   * Describes the prerequisites in the build environment for
   * this project.
   */
  private Prerequisites prerequisites;

  /**
   * Specification for the SCM used by the project, such as CVS,
   * Subversion, etc.
   */
  private Scm scm;

  /**
   * The project's issue management system information.
   */
  private IssueManagement issueManagement;

  /**
   * The project's continuous integration information.
   */
  private CiManagement ciManagement;

  /**
   * Information required to build the project.
   */
  private Build build;

  /**
   * Field profiles.
   */
  private java.util.List<Profile> profiles;

  /**
   * Field modelEncoding.
   */
  private String modelEncoding = "UTF-8";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSubType() {
    return subType;
  }

  public void setSubType(String subType) {
    this.subType = subType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getModelVersion() {
    return modelVersion;
  }

  public void setModelVersion(String modelVersion) {
    this.modelVersion = modelVersion;
  }

  public Parent getParent() {
    return parent;
  }

  public void setParent(Parent parent) {
    this.parent = parent;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getChildInheritAppendPath() {
    return childInheritAppendPath;
  }

  public void setChildInheritAppendPath(String childInheritAppendPath) {
    this.childInheritAppendPath = childInheritAppendPath;
  }

  public String getInceptionYear() {
    return inceptionYear;
  }

  public void setInceptionYear(String inceptionYear) {
    this.inceptionYear = inceptionYear;
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  public List<License> getLicenses() {
    return licenses;
  }

  public void setLicenses(List<License> licenses) {
    this.licenses = licenses;
  }

  public List<Developer> getDevelopers() {
    return developers;
  }

  public void setDevelopers(List<Developer> developers) {
    this.developers = developers;
  }

  public List<Contributor> getContributors() {
    return contributors;
  }

  public void setContributors(List<Contributor> contributors) {
    this.contributors = contributors;
  }

  public List<MailingList> getMailingLists() {
    return mailingLists;
  }

  public void setMailingLists(List<MailingList> mailingLists) {
    this.mailingLists = mailingLists;
  }

  public Prerequisites getPrerequisites() {
    return prerequisites;
  }

  public void setPrerequisites(Prerequisites prerequisites) {
    this.prerequisites = prerequisites;
  }

  public Scm getScm() {
    return scm;
  }

  public void setScm(Scm scm) {
    this.scm = scm;
  }

  public IssueManagement getIssueManagement() {
    return issueManagement;
  }

  public void setIssueManagement(IssueManagement issueManagement) {
    this.issueManagement = issueManagement;
  }

  public CiManagement getCiManagement() {
    return ciManagement;
  }

  public void setCiManagement(CiManagement ciManagement) {
    this.ciManagement = ciManagement;
  }

  public Build getBuild() {
    return build;
  }

  public void setBuild(Build build) {
    this.build = build;
  }

  public List<Profile> getProfiles() {
    return profiles;
  }

  public void setProfiles(List<Profile> profiles) {
    this.profiles = profiles;
  }

  public String getModelEncoding() {
    return modelEncoding;
  }

  public void setModelEncoding(String modelEncoding) {
    this.modelEncoding = modelEncoding;
  }
}
