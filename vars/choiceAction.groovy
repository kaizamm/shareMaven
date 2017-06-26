def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  def actionInput = input (
    id: 'actionInput', message: 'Choice your action!', parameters: [[$class: 'ChoiceParameterDefinition', choices: "deploy\nrollback", description: 'choice your action!', name: 'action']]
    )
  def action = actionInput.trim()
}
