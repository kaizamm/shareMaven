def call() {
  /*
  // def actionInput = input (
  //   id: 'actionInput', message: 'Choice your action!', parameters: [
  //   [$class: 'ChoiceParameterDefinition', choices: "deploy\nrollback", description: 'choice your action!', name: 'action']])
  // def action = actionInput.trim()
  */
  // 目前定义的动作是deploy和rollback，后期可以看情况添加container start或container stop等动作
  def actionInput = input (
    id: 'actionInput', message: 'Choice your action!', parameters: [[$class: 'ChoiceParameterDefinition', choices: "deploy\nrollback", description: 'choice your action!', name: 'action']]
    )
  return actionInput.trim()
}
