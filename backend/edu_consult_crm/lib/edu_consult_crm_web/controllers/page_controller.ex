defmodule EduConsultCrmWeb.PageController do
  use EduConsultCrmWeb, :controller

  def home(conn, _params) do
    render(conn, :home)
  end
end
