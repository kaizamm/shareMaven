def call() {
  def actionInput = input (
    id: 'actionInput', message: 'Choice your action!', parameters: [[$class: 'ChoiceParameterDefinition', choices: "deploy\nrollback", description: 'choice your action!', name: 'action']]
    )
  return action = actionInput.trim()
}
