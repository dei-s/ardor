package com.jelurida.contract.manager.plugin;

import com.intellij.application.options.ModuleDescriptionsComboBox;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.execution.ui.ClassBrowser;
import com.intellij.execution.ui.CommonJavaParametersPanel;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.execution.ui.JrePathEditor;
import com.intellij.execution.ui.ShortenCommandLineModeCombo;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.UIUtil;
import nxt.tools.ContractManager;
import nxt.util.Convert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ContractManagerConfigurable extends SettingsEditor<ApplicationConfiguration> implements PanelWithAnchor {
    private static final String VM_PARAMS_SECURITY_POLICY = "-Djava.security.manager -Djava.security.policy=contractManager.policy";

    // Fields copied from the IntelliJ default ApplicationConfigurable
    private CommonJavaParametersPanel myCommonProgramParameters;
    private LabeledComponent<EditorTextFieldWithBrowseButton> myMainClass;
    private LabeledComponent<ModuleDescriptionsComboBox> myModule;
    private LabeledComponent<ShortenCommandLineModeCombo> myShortenClasspathModeCombo;
    private JPanel myWholePanel;
    private final ConfigurationModuleSelector myModuleSelector;
    private JrePathEditor myJrePathEditor;
    private JCheckBox myShowSwingInspectorCheckbox;
    private LabeledComponent<JBCheckBox> myIncludeProvidedDeps;
    private final Project myProject;
    private JComponent myAnchor;

    // Fields specific to the contract manager plugin
    private JComboBox<String> cmActionSelection;
    private LabeledComponent<EditorTextFieldWithBrowseButton> myContractClass;
    private JTextField cmContractName;
    private JTextField cmContractPackage;
    private JTextField cmContractHash;
    private JTextField cmReferenceAccount;
    private TextFieldWithBrowseButton cmContractSourceToVerify;

    // Mapper between the UI fields and the contract manager command line options
    private List<OptionData> cmOptions = new ArrayList<>();

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        myWholePanel = new JPanel();
        myWholePanel.setLayout(new GridLayoutManager(14, 2, new Insets(0, 0, 0, 0), -1, -1));
        myWholePanel.add(myCommonProgramParameters, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myModule.setLabelLocation("West");
        myModule.setText(ResourceBundle.getBundle("messages/ExecutionBundle").getString("application.configuration.use.classpath.and.jdk.of.module.label"));
        myWholePanel.add(myModule, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myJrePathEditor = new JrePathEditor();
        myWholePanel.add(myJrePathEditor, new GridConstraints(10, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        myShowSwingInspectorCheckbox = new JCheckBox();
        this.$$$loadButtonText$$$(myShowSwingInspectorCheckbox, ResourceBundle.getBundle("messages/ExecutionBundle").getString("show.swing.inspector"));
        myWholePanel.add(myShowSwingInspectorCheckbox, new GridConstraints(12, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        myWholePanel.add(spacer1, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 10), null, 0, false));
        final Spacer spacer2 = new Spacer();
        myWholePanel.add(spacer2, new GridConstraints(13, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 10), null, 0, false));
        myShortenClasspathModeCombo.setLabelLocation("West");
        myShortenClasspathModeCombo.setText(ResourceBundle.getBundle("messages/ExecutionBundle").getString("application.configuration.shorten.command.line.label"));
        myWholePanel.add(myShortenClasspathModeCombo, new GridConstraints(11, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myIncludeProvidedDeps = new LabeledComponent();
        myIncludeProvidedDeps.setLabelLocation("West");
        myIncludeProvidedDeps.setText("");
        myWholePanel.add(myIncludeProvidedDeps, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        myWholePanel.add(panel1, new GridConstraints(1, 0, 4, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Action:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cmActionSelection = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("upload");
        defaultComboBoxModel1.addElement("reference");
        defaultComboBoxModel1.addElement("delete");
        defaultComboBoxModel1.addElement("list");
        defaultComboBoxModel1.addElement("verify");
        cmActionSelection.setModel(defaultComboBoxModel1);
        panel1.add(cmActionSelection, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JLabel label2 = new JLabel();
        label2.setText("Contract Name:");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(cmContractName, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Contract Hash:");
        panel1.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(cmContractHash, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Runner Account:");
        panel1.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(cmReferenceAccount, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Contract Source:");
        panel1.add(label5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(cmContractSourceToVerify, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 10), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Contract Pakcgae:");
        panel1.add(label6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(cmContractPackage, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        myMainClass.setLabelLocation("West");
        myMainClass.setText(ResourceBundle.getBundle("messages/ExecutionBundle").getString("application.configuration.main.class.label"));
        myWholePanel.add(myMainClass, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 20), null, 0, false));
        myContractClass.setLabelLocation("West");
        myContractClass.setText("Contract Class");
        myWholePanel.add(myContractClass, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 20), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return myWholePanel;
    }

    /**
     * Provides data binding between the UI fields and the command line options for the contract manager
     */
    private static class OptionData {
        public static final String QUOTE = "\"";
        private final ContractManager.OPTION option;
        private final JTextField dataProvider;
        private final boolean isQuote;

        OptionData(ContractManager.OPTION option, JTextField dataProvider, boolean isQuote) {
            this.option = option;
            this.dataProvider = dataProvider;
            this.isQuote = isQuote;
        }

        String getToken() {
            return "--" + option.getLongOpt() + " " + getText();
        }

        boolean isEnabled() {
            return dataProvider.isEnabled();
        }

        boolean hasText() {
            return getText() != null && !getText().equals("");
        }

        private String getText() {
            String text = dataProvider.getText();
            text = doQuote(text);
            return text;
        }

        private void setText(String text) {
            text = doQuote(text);
            dataProvider.setText(text);
        }

        private String doQuote(String text) {
            if (isQuote) {
                if (text != null && !text.startsWith(QUOTE) && !text.endsWith(QUOTE)) {
                    text = QUOTE + text + QUOTE;
                }
            }
            return text;
        }
    }

    /**
     * Constructs the dialog page
     *
     * @param project the IntelliJ project
     */
    ContractManagerConfigurable(final Project project) {
        myProject = project;
        $$$setupUI$$$();

        // Runs after the UI components were already created
        myModuleSelector = new ConfigurationModuleSelector(project, myModule.getComponent());
        myJrePathEditor.setDefaultJreSelector(DefaultJreSelector.fromSourceRootsDependencies(myModule.getComponent(), myMainClass.getComponent()));
        myCommonProgramParameters.setModuleContext(myModuleSelector.getModule());
        myModule.getComponent().addActionListener(e -> myCommonProgramParameters.setModuleContext(myModuleSelector.getModule()));

        // Init the contract class selector
        ClassBrowser applicationClassBrowser = createContractClassBrowser(project, myModuleSelector);

        //noinspection unchecked
        applicationClassBrowser.setField(myContractClass.getComponent());

        myShortenClasspathModeCombo.setComponent(new ShortenCommandLineModeCombo(myProject, myJrePathEditor, myModule.getComponent()));
        myIncludeProvidedDeps.setComponent(new JBCheckBox(ExecutionBundle.message("application.configuration.include.provided.scope")));
        myAnchor = UIUtil.mergeComponentsWithAnchor(myMainClass, myCommonProgramParameters, myJrePathEditor, myModule,
                myShortenClasspathModeCombo, myIncludeProvidedDeps);

        // Registers a listener on the action selection field
        cmActionSelection.addActionListener(e -> cmActionChanged());
    }

    /**
     * Given the current action selection and dialog values, configure the contract manager command line and enable/disable the fields relevant to this action
     */
    private void cmActionChanged() {
        String action = (String) cmActionSelection.getSelectedItem();
        ContractManager.OPTION option = Arrays.stream(ContractManager.OPTION.values()).filter(o -> o.getLongOpt().equals(action)).findFirst().orElse(null);
        List<ContractManager.OPTION> dependencies = option != null && option.getDependencies() != null ? Arrays.asList(option.getDependencies()) : Collections.emptyList();
        changeOption(dependencies, cmContractName, ContractManager.OPTION.NAME);
        changeOption(dependencies, cmContractPackage, ContractManager.OPTION.PACKAGE);
        changeOption(dependencies, cmContractHash, ContractManager.OPTION.HASH);
        changeOption(dependencies, cmReferenceAccount, ContractManager.OPTION.ACCOUNT);
        changeOption(dependencies, cmContractSourceToVerify.getTextField(), ContractManager.OPTION.SOURCE);
        String params = cmOptions.stream().filter(OptionData::isEnabled).filter(OptionData::hasText).map(OptionData::getToken).collect(Collectors.joining(" "));

        // Patch for the contract class selection
        myContractClass.setEnabled(cmContractName.isEnabled());
        cmContractName.setEnabled(false);
        cmContractPackage.setEnabled(false);

        // Set the contract manager command line
        myCommonProgramParameters.setProgramParameters("--" + action + " " + params);

        if ("verify".equals(cmActionSelection.getSelectedItem())) {
            if (myCommonProgramParameters.getVMParameters() == null || myCommonProgramParameters.getVMParameters().equals("")) {
                myCommonProgramParameters.setVMParameters(VM_PARAMS_SECURITY_POLICY);
            }
        } else {
            if (myCommonProgramParameters.getVMParameters() != null && myCommonProgramParameters.getVMParameters().equals(VM_PARAMS_SECURITY_POLICY)) {
                myCommonProgramParameters.setVMParameters("");
            }
        }
    }

    /**
     * Enable/Disable a field based on the current action dependencies and the option it represents
     *
     * @param dependencies the current action dependecies
     * @param field        the field
     * @param option       the command line option it represents
     */
    private void changeOption(List<ContractManager.OPTION> dependencies, JTextField field, ContractManager.OPTION option) {
        if (dependencies.contains(option)) {
            field.setEnabled(true);
        } else {
            field.setEnabled(false);
            field.setText("");
        }
    }

    /**
     * Invoked during initialization and on every key stroke
     *
     * @param configuration the panel configuration
     */
    @Override
    public void applyEditorTo(@NotNull final ApplicationConfiguration configuration) {
        myCommonProgramParameters.applyTo(configuration);
        myModuleSelector.applyTo(configuration);
        configuration.setAlternativeJrePath(myJrePathEditor.getJrePathOrName());
        configuration.setAlternativeJrePathEnabled(myJrePathEditor.isAlternativeJreSelected());
        configuration.setSwingInspectorEnabled((myShowSwingInspectorCheckbox.isSelected()));
        configuration.setShortenCommandLine(myShortenClasspathModeCombo.getComponent().getSelectedItem());
        configuration.setIncludeProvidedScope(myIncludeProvidedDeps.getComponent().isSelected());
        myShowSwingInspectorCheckbox.setVisible(false);
    }

    /**
     * Invoked during initialization of new or existing launcher
     *
     * @param configuration the panel configuration
     */
    @Override
    public void resetEditorFrom(@NotNull final ApplicationConfiguration configuration) {
        myCommonProgramParameters.reset(configuration);
        myModuleSelector.reset(configuration);
        if (configuration.getMainClassName() == null) {
            String className = "nxt.tools.ContractManager";
            configuration.setMainClassName(className);
            myMainClass.getComponent().setText(className);
        }
        if (configuration.getProgramParameters() == null) {
            String action = "--" + cmActionSelection.getSelectedItem();
            configuration.setProgramParameters(action);
            myCommonProgramParameters.setProgramParameters(action);
        } else {
            // Since we do not save the contract manager parameters separately in the configuration we need to parse
            // them from the program arguments (this is pretty ugly and will change in the future)
            parseProgramArguments(configuration.getProgramParameters());
        }
        myMainClass.getComponent().setText(configuration.getMainClassName() != null ? configuration.getMainClassName().replaceAll("\\$", "\\.") : "");
        myJrePathEditor.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
        myShortenClasspathModeCombo.getComponent().setSelectedItem(configuration.getShortenCommandLine());
        myIncludeProvidedDeps.getComponent().setSelected(configuration.isProvidedScopeIncluded());
        myShowSwingInspectorCheckbox.setVisible(false);
        cmActionChanged();
    }

    /**
     * When loading existing configuration, parse the program arguments and update the UI fields
     *
     * @param programArguments the value of the program arguments field
     */
    private void parseProgramArguments(@NotNull String programArguments) {
        // We assume that the first param is the action
        String[] params = programArguments.split("--");
        if (params.length <= 1 || !params[0].equals("")) {
            return;
        }
        String param = params[1].trim();
        if (Arrays.stream(ContractManager.OPTION.values()).anyMatch(o -> o.getLongOpt().equals(param))) {
            cmActionSelection.setSelectedItem(param);
        }
        if (params.length <= 2) {
            return;
        }
        for (int i = 2; i < params.length; i++) {
            int splitPoint = params[i].indexOf(" ");
            if (splitPoint == -1) {
                continue; // Not a parameter with value
            }
            final String optionKey = params[i].substring(0, splitPoint);
            final String optionValue = params[i].substring(splitPoint + 1).trim();
            cmOptions.stream().filter(o -> o.option.getLongOpt().equals(optionKey)).forEach(o -> o.setText(optionValue));
        }

        // Update the contract class name
        if (Convert.emptyToNull(cmContractPackage.getText()) != null && Convert.emptyToNull(cmContractName.getText()) != null) {
            EditorTextFieldWithBrowseButton component = myContractClass.getComponent();
            EditorTextField editorTextField = component.getChildComponent();
            editorTextField.setText(cmContractPackage.getText() + "." + cmContractName.getText());
        }
    }

    /**
     * Invoked during initialization
     *
     * @return the whole panel
     */
    @Override
    @NotNull
    public JComponent createEditor() {
        return myWholePanel;
    }

    /**
     * Invoked before UI Initialization. Components which has Custom Create checked, should be created here.
     */
    private void createUIComponents() {
        myMainClass = new LabeledComponent<>();
        EditorTextFieldWithBrowseButton editorComponent = new EditorTextFieldWithBrowseButton(myProject, true, JavaCodeFragment.VisibilityChecker.PROJECT_SCOPE_VISIBLE);
        editorComponent.setEnabled(false);
        myMainClass.setComponent(editorComponent);

        myContractClass = new LabeledComponent<>();
        EditorTextFieldWithBrowseButton myContractClassEditorComponent = new EditorTextFieldWithBrowseButton(myProject, true, JavaCodeFragment.VisibilityChecker.PROJECT_SCOPE_VISIBLE);
        myContractClass.setComponent(myContractClassEditorComponent);
        myShortenClasspathModeCombo = new LabeledComponent<>();
        myModule = new LabeledComponent<>();
        try {
            myModule.setComponentClass("com.intellij.application.options.ModuleDescriptionsComboBox");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        myCommonProgramParameters = new CommonJavaParametersPanel();
        cmContractName = initOptionField(ContractManager.OPTION.NAME);
        cmContractPackage = initOptionField(ContractManager.OPTION.PACKAGE);
        cmContractHash = initOptionField(ContractManager.OPTION.HASH);
        cmReferenceAccount = initOptionField(ContractManager.OPTION.ACCOUNT);
        cmContractSourceToVerify = new TextFieldWithBrowseButton();
        cmOptions.add(new OptionData(ContractManager.OPTION.SOURCE, cmContractSourceToVerify.getTextField(), true));
        cmContractSourceToVerify.addBrowseFolderListener("Choose Java Source File",
                "Source file will be compiled and compared with contract code deployed to the blockchain",
                myProject,
                FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(JavaFileType.INSTANCE),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        addChangeListener(cmContractSourceToVerify.getTextField(), e -> cmActionChanged());
        EditorTextFieldWithBrowseButton component = myContractClass.getComponent();
        EditorTextField editorTextField = component.getChildComponent();
        addChangeListener(editorTextField, e -> cmActionChanged());
    }

    /**
     * Create field and associate it with the command line option it represents
     *
     * @param option the command line option
     * @return the initialized field
     */
    private JTextField initOptionField(ContractManager.OPTION option) {
        JTextField field = new JTextField();
        cmOptions.add(new OptionData(option, field, false));
        addChangeListener(field, e -> cmActionChanged());
        return field;
    }

    /**
     * I'm still not sure what the purpose of the anchor. We are not using it.
     *
     * @return the anchor
     */
    @Override
    public JComponent getAnchor() {
        return myAnchor;
    }

    /**
     * I'm still not sure what the purpose of the anchor. We are not using it.
     */
    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        this.myAnchor = anchor;
        myMainClass.setAnchor(anchor);
        myCommonProgramParameters.setAnchor(anchor);
        myJrePathEditor.setAnchor(anchor);
        myModule.setAnchor(anchor);
        myShortenClasspathModeCombo.setAnchor(anchor);
    }

    String getCmContractHash() {
        return cmContractHash.getText();
    }

    String getCmReferenceAccount() {
        return cmReferenceAccount.getText();
    }

    /**
     * Installs a listener to receive notification when the text of any {@code JTextComponent} is changed.
     * Internally, it installs a {@link DocumentListener} on the text component's {@link Document},
     * and a {@link PropertyChangeListener} on the text component to detect if the {@code Document} itself
     * is replaced.
     *
     * @param text           any text component, such as a {@link JTextField} or {@link JTextArea}
     * @param changeListener a listener to receieve {@link ChangeEvent}s when the text is changed;
     *                       the source object for the events will be the text component
     * @throws NullPointerException if either parameter is null
     */
    public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(changeListener);
        DocumentListener dl = new DocumentListener() {
            private int lastChange = 0, lastNotifiedChange = 0;

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastChange++;
                SwingUtilities.invokeLater(() -> {
                    if (lastNotifiedChange != lastChange) {
                        lastNotifiedChange = lastChange;
                        changeListener.stateChanged(new ChangeEvent(text));
                    }
                });
            }
        };
        text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
            Document d1 = (Document) e.getOldValue();
            if (d1 != null) {
                d1.removeDocumentListener(dl);
            }
            Document d2 = (Document) e.getNewValue();
            if (d2 != null) {
                d2.addDocumentListener(dl);
            }
            dl.changedUpdate(null);
        });
        Document d = text.getDocument();
        if (d != null) {
            d.addDocumentListener(dl);
        }
    }

    /**
     * Installs a listener to receive notification when the text of any {@code EditorTextField} is changed.
     *
     * @param field          the editor text field
     * @param changeListener a listener to receieve {@link ChangeEvent}s when the text is changed;
     *                       the source object for the events will be the text component
     * @throws NullPointerException if either parameter is null
     */
    public void addChangeListener(EditorTextField field, ChangeListener changeListener) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(changeListener);
        com.intellij.openapi.editor.event.DocumentListener dl = new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(com.intellij.openapi.editor.event.DocumentEvent event) {
                // Split the class name entered by the user into package name and simple class name and update the read only fields
                // from which the command line option is built.
                String contractClassFullName = field.getText();
                int splitPoint = contractClassFullName.lastIndexOf(".");
                if (splitPoint == -1) {
                    ContractManagerConfigurable.this.cmContractName.setText(contractClassFullName);
                    ContractManagerConfigurable.this.cmContractPackage.setText("");
                } else {
                    ContractManagerConfigurable.this.cmContractPackage.setText(contractClassFullName.substring(0, splitPoint));
                    if (field.getText().length() > splitPoint) {
                        ContractManagerConfigurable.this.cmContractName.setText(contractClassFullName.substring(splitPoint + 1));
                    } else {
                        ContractManagerConfigurable.this.cmContractName.setText("");
                    }
                }
                changeListener.stateChanged(new ChangeEvent(field));
            }
        };
        field.getDocument().addDocumentListener(dl);
    }

    public static ClassBrowser createContractClassBrowser(final Project project, final ConfigurationModuleSelector moduleSelector) {
        final String title = ContractManagerBundle.message("choose.contract.class.dialog.title");
        return new ClassBrowser.MainClassBrowser(project, moduleSelector, title) {

            @Override
            protected TreeClassChooser createClassChooser(ClassFilter.ClassFilterWithScope classFilter) {
                final Module module = moduleSelector.getModule();
                final GlobalSearchScope scope = module == null ? GlobalSearchScope.allScope(myProject) : GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
                final PsiClass contractClass = JavaPsiFacade.getInstance(project).findClass("nxt.addons.AbstractContract", scope);
                return TreeClassChooserFactory.getInstance(getProject()).createInheritanceClassChooser(title, classFilter.getScope(), contractClass, false, false, ConfigurationUtil.PUBLIC_INSTANTIATABLE_CLASS);
            }
        };
    }
}
